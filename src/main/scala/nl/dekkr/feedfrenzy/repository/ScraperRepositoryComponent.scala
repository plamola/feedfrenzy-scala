package nl.dekkr.feedfrenzy.repository

import nl.dekkr.feedfrenzy.model.{PageCache, Scraper, ScraperAction}

/**
 * Author: Matthijs Dekker
 * Created on: 26 Jan 2015
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
