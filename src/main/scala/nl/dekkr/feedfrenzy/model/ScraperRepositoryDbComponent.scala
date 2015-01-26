package nl.dekkr.feedfrenzy.model

import nl.dekkr.feedfrenzy.db.{ Tables, Schema }

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery

/**
 * Created by Matthijs Dekker on 21/01/15.
 */
trait ScraperRepositoryDbComponent extends ScraperRepositoryComponent {

  def scraperRepository = new ScraperRepositoryImpl

  class ScraperRepositoryImpl extends ScraperRepository {

    def findUpdatable = getRunnableScrapers

    def getRunnableScrapers: List[Scraper] = {
      implicit val session = Schema.getSession
      val feeds = TableQuery[Tables.FeedTable]
      try {
        val feeds = TableQuery[Tables.FeedTable]
        Schema.createOrUpdate(session)
        addDummyContent()
        feeds.list.map(feed =>
          Scraper(id = feed.id, sourceUrl = feed.feedurl, singlePage = true)
        )
      } catch {
        case e: Exception =>
          println(s"ERROR: ${e.getMessage} [${e.getCause}]")
          List.empty
      }
    }

    def addDummyContent() {
      implicit val session = Schema.getSession
      val feeds = TableQuery[Tables.FeedTable]
      Schema.createOrUpdate(session)

      // Add some dummy feeds
      if (feeds.list.size < 1)
        feeds += Feed(feedurl = "http://nu.nl")

      if (feeds.list.size < 2)
        feeds += Feed(feedurl = "http://dagartikel.nl")

      if (feeds.list.size < 3)
        feeds += Feed(feedurl = "http://news.google.com")

      if (feeds.list.size < 4)
        feeds += Feed(feedurl = "https://news.yahoo.com")

      if (feeds.list.size < 5)
        feeds += Feed(feedurl = "http://www.theverge.com/")

      if (feeds.list.size < 6)
        feeds += Feed(feedurl = "https://news.ycombinator.com/")

    }

  }
}
