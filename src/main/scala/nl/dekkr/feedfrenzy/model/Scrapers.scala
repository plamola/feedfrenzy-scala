package nl.dekkr.feedfrenzy.model

import nl.dekkr.feedfrenzy.repository.ScraperRepositoryComponent

/**
 * Created by Matthijs Dekker on 26/01/15.
 */
trait Scrapers {
  this: ScraperRepositoryComponent =>

  def getUpdatable = scraperRepository.findUpdatable
  def getActions(id: Int) = scraperRepository.findActions(id)

}
