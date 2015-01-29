package nl.dekkr.feedfrenzy.model

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
