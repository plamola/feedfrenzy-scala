package nl.dekkr.feedfrenzy.model

/**
 * Created by Matthijs Dekker on 26/01/15.
 */
case class ContentBlock(
  scraperDefinition: ScraperDefinition,
  sourceUrl: Option[String] = None,
  pageContent: Option[String] = None,
  result: Option[ScrapeResult] = None)
