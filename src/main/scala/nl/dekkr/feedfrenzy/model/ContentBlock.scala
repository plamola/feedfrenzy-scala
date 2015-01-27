package nl.dekkr.feedfrenzy.model

/**
 * Created by Matthijs Dekker on 26/01/15.
 */
case class ContentBlock(scraper: Scraper, content: Option[String] = None, uid: Option[String] = None, uri: Option[String] = None)