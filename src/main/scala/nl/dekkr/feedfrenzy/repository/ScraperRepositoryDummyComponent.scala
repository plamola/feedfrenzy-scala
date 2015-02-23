package nl.dekkr.feedfrenzy.repository

import nl.dekkr.feedfrenzy.model._
import org.joda.time.DateTime

/**
 * Created by Matthijs Dekker on 21/01/15.
 */
trait ScraperRepositoryDummyComponent extends ScraperRepositoryComponent {

  def scraperRepository = new ScraperRepositoryImpl

  class ScraperRepositoryImpl extends ScraperRepository {
    //def findAll = List.empty[Scraper]
    def findUpdatable = List(
      Scraper(id = Some(10), sourceUrl = "http://rtvutrecht.nl", singlePage = false),
      Scraper(id = Some(20), sourceUrl = "http://dagartikel.nl", singlePage = true),
      Scraper(id = Some(30), sourceUrl = "http://ad.nl", singlePage = false),
      Scraper(id = Some(40), sourceUrl = "http://wehkamp.nl", singlePage = false)
    )

    def findActions(scraperId: Int) = List(
      ScraperAction(None, Some(scraperId), ActionPhase.INDEX, 1, ActionType.CSS_SELECTOR, None, Some("div.actueel div.actueel-text p a"), None),

      ScraperAction(None, Some(scraperId), ActionPhase.UIDS, 1, ActionType.ATTRIBUTE, Some("block"), Some("href"), Some("uri")),
      ScraperAction(None, Some(scraperId), ActionPhase.UIDS, 2, ActionType.REGEX, Some("uri"), Some("(?!/nieuws/)(\\\\d+)"), Some("uid")),
      ScraperAction(None, Some(scraperId), ActionPhase.UIDS, 3, ActionType.TEMPLATE, None, Some("http://www.rtvutrecht.nl/nieuws/{uid}"), Some("feeditem_url")),
      ScraperAction(None, Some(scraperId), ActionPhase.UIDS, 4, ActionType.TEMPLATE, None, Some("{feeditem_url}"), Some("feeditem_uid")),

      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 1, ActionType.CSS_SELECTOR, Some("contentBody"), Some("div#Midden>p"), Some("content")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 2, ActionType.CSS_SELECTOR, Some("contentBody"), Some("div#Content h1.border-top"), Some("feeditem_title")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 3, ActionType.CSS_SELECTOR, Some("contentBody"), Some("div#Content p.verslaggever"), Some("verslaggeverregel")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 4, ActionType.REGEX, Some("verslaggeverregel"), Some("(.*)(?=\\&middot;)"), Some("feeditem_author")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 5, ActionType.REGEX, Some("verslaggeverregel"), Some("(\\d{1,2}\\s\\w+\\s\\d{4})"), Some("datumstring")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 6, ActionType.REGEX, Some("verslaggeverregel"), Some("(\\d{1,2}\\:\\d{2})"), Some("tijdstring")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 7, ActionType.CSS_SELECTOR, Some("contentBody"), Some("div#Midden div#Carousel div.carousel-inner div.active"), Some("image")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 8, ActionType.CSS_SELECTOR_REMOVE, Some("image"), Some("div.carousel-caption"), Some("imageZonderCaption")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 9, ActionType.CSS_SELECTOR_PARENT, Some("contentBody"), Some("div#Midden>img"), Some("inlineImage")),
      ScraperAction(None, Some(scraperId), ActionPhase.CONTENT, 10, ActionType.TEMPLATE, None, Some("<p>{imageZonderCaption}{inlineImage}</p>{content}"), Some("feeditem_content"))
    )

    def findPage(uri: String) : Option[PageCache] = {
      Some(
        PageCache(
          uri = uri,
          createdAt = Some(DateTime.now())
        )
      )
    }

    def cachePage(uri: String, content: Option[String]) {
      PageCache(
        uri = uri,
        content = content,
        createdAt = Some(DateTime.now())
      )
    }

  }

}
