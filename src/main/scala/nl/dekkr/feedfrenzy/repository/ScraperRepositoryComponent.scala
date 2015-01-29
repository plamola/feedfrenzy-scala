package nl.dekkr.feedfrenzy.repository

import nl.dekkr.feedfrenzy.model.{ Scraper, ScraperAction }

/**
 * Created by Matthijs Dekker on 21/01/15.
 */
trait ScraperRepositoryComponent {

  def scraperRepository: ScraperRepository

  trait ScraperRepository {
    def findUpdatable: List[Scraper]
    def findActions(scraperId: Int): List[ScraperAction]
  }

}
