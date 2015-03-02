package nl.dekkr.feedfrenzy.model

import nl.dekkr.feedfrenzy.repository.ScraperRepositoryComponent

/**
 * Author: Matthijs Dekker
 * Created on: 26 Jan 2015.
 */
trait Scrapers {
  this: ScraperRepositoryComponent =>

  def getUpdatable = scraperRepository.findUpdatable
  def getActions(id: Int) = scraperRepository.findActions(id)

}
