package nl.dekkr.feedfrenzy.model

/**
 * Author: Matthijs Dekker
 * Created on: 26 Jan 2015.
 */
case class Scraper(id: Option[Int] = None, sourceUrl: String, singlePage: Boolean = false)
