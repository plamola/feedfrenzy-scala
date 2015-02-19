package nl.dekkr.feedfrenzy.repository

import nl.dekkr.feedfrenzy.model._
import org.joda.time.DateTime

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery

/**
 * Created by Matthijs Dekker on 21/01/15.
 */
trait ScraperRepositoryDbComponent extends ScraperRepositoryComponent {

  def scraperRepository = new ScraperRepositoryImpl

  class ScraperRepositoryImpl extends ScraperRepository {

    def findUpdatable = getRunnableScrapers

    def findActions(scraperId: Int) = getScraperActions(scraperId)

    private def getRunnableScrapers: List[Scraper] = {
      implicit val session = Schema.getSession
      try {
        val scrapers = TableQuery[Tables.ScraperTable]
        val feeds = TableQuery[Tables.FeedTable]
        Schema.createOrUpdate(session)
        addDummyContent()
        val activeScrapers = for {
          f <- feeds if f.nextupdate < DateTime.now().getMillis
          s <- scrapers if s.id === f.scraperid
        } yield s
        activeScrapers.list
      } catch {
        case e: Exception =>
          println(s"ERROR: ${e.getMessage} [${e.getCause}]")
          List.empty
      }
    }

    private def getScraperActions(scraperId: Int): List[ScraperAction] = {
      implicit val session = Schema.getSession
      try {
        val scraperActions = TableQuery[Tables.ScraperActionTable]
        val actions = for {
          a <- scraperActions if a.scraperid === scraperId
        } yield a
        actions.sortBy(s => (s.actionPhase.asc, s.actionOrder.asc)).list
      } catch {
        case e: Exception =>
          println(s"ERROR: ${e.getMessage} [${e.getCause}]")
          List.empty
      }
    }

    private def addDummyContent() {
      implicit val session = Schema.getSession
      val feeds = TableQuery[Tables.FeedTable]
      val scrapers = TableQuery[Tables.ScraperTable]
      val scraperActions = TableQuery[Tables.ScraperActionTable]

      Schema.createOrUpdate(session)
      // Add some dummy scrapers
      if (scrapers.list.size < 1)
        scrapers += Scraper(sourceUrl = "http://nu.nl")

      if (scrapers.list.size < 2)
        scrapers += Scraper(sourceUrl = "http://dagartikel.nl", singlePage = true)

      if (scrapers.list.size < 3)
        scrapers += Scraper(sourceUrl = "http://rtvutrecht.nl")

      // Add some dummy scrapers actions
      val scraperId = Some(3)

      if (scraperActions.list.size < 1) {
        scraperActions ++= Seq(
          ScraperAction(None, scraperId, ActionPhase.INDEX, 1, ActionType.CSS_SELECTOR_PARENT, Some("contentBody"), Some("div.actueel div.actueel-text p a"), Some("contentBody")),

          ScraperAction(None, scraperId, ActionPhase.UIDS, 1, ActionType.ATTRIBUTE, Some("block"), Some("href"), Some("uri")),
          ScraperAction(None, scraperId, ActionPhase.UIDS, 2, ActionType.REGEX, Some("uri"), Some("(?!/nieuws/)(\\d+)"), Some("uid")),
          ScraperAction(None, scraperId, ActionPhase.UIDS, 3, ActionType.TEMPLATE, None, Some("http://www.rtvutrecht.nl/nieuws/{uid}"), Some("feeditem_url")),
          ScraperAction(None, scraperId, ActionPhase.UIDS, 4, ActionType.TEMPLATE, None, Some("{feeditem_url}"), Some("feeditem_uid")),

          ScraperAction(None, scraperId, ActionPhase.CONTENT, 1, ActionType.CSS_SELECTOR, Some("contentBody"), Some("div#Midden>p"), Some("content")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 2, ActionType.CSS_SELECTOR, Some("contentBody"), Some("div#Content h1.border-top"), Some("feeditem_title")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 3, ActionType.CSS_SELECTOR, Some("contentBody"), Some("div#Content p.verslaggever"), Some("verslaggeverregel")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 4, ActionType.REGEX, Some("verslaggeverregel"), Some("(.*)(?=\\s.\\sgeplaatst)"), Some("feeditem_author")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 5, ActionType.REGEX, Some("verslaggeverregel"), Some("(\\d{1,2}\\s\\w+\\s\\d{4})"), Some("datumstring")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 6, ActionType.REGEX, Some("verslaggeverregel"), Some("(\\d{1,2}\\:\\d{2})"), Some("tijdstring")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 7, ActionType.CSS_SELECTOR, Some("contentBody"), Some("div#Midden div#Carousel div.carousel-inner div.active"), Some("image")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 8, ActionType.CSS_SELECTOR_REMOVE, Some("image"), Some("div.carousel-caption"), Some("imageZonderCaption")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 9, ActionType.CSS_SELECTOR_PARENT, Some("contentBody"), Some("div#Midden>img"), Some("inlineImage")),
          ScraperAction(None, scraperId, ActionPhase.CONTENT, 10, ActionType.TEMPLATE, None, Some("<p>{imageZonderCaption}{inlineImage}</p>{content}"), Some("feeditem_content"))
        )
      }

      // Add some dummy feeds
      if (feeds.list.size < 1)
        feeds += Feed(feedurl = "http://nu.nl")

      if (feeds.list.size < 2)
        feeds += Feed(feedurl = "http://dagartikel.nl")

      if (feeds.list.size < 3)
        feeds += Feed(feedurl = "http://news.google.com")

      if (feeds.list.size < 4)
        feeds += Feed(feedurl = "http://rtvutrecht.nl", scraperId = scraperId)

      if (feeds.list.size < 5)
        feeds += Feed(feedurl = "http://www.theverge.com/")

      if (feeds.list.size < 6)
        feeds += Feed(feedurl = "https://news.ycombinator.com/")

    }

  }

}
