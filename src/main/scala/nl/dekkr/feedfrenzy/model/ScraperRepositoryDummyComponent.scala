package nl.dekkr.feedfrenzy.model

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
  }

}
