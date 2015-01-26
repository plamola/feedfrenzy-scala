package nl.dekkr.feedfrenzy.model

/**
 * Created by Matthijs Dekker on 26/01/15.
 */
case class Scraper(id: Option[Int] = None, sourceUrl: String, singlePage: Boolean = false)
