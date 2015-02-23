package nl.dekkr.feedfrenzy.repository

import nl.dekkr.feedfrenzy.model.{PageCache, Scraper, ScraperAction}

/**
 * Created by Matthijs Dekker on 21/01/15.
 */
trait ScraperRepositoryComponent {

  def scraperRepository: ScraperRepository

  trait ScraperRepository {
    def findUpdatable: List[Scraper]
    def findActions(scraperId: Int): List[ScraperAction]
    def findPage(uri: String) : Option[PageCache]
    def cachePage(uri: String, content: Option[String])
  }

}
