package nl.dekkr.feedfrenzy.model

/**
 * Entity to contain scraped content, including the method of appropriating the content
 */
case class ContentBlock(
  scraperDefinition: ScraperDefinition,
  sourceUrl: Option[String] = None,
  pageContent: Option[String] = None,
  result: Option[ScrapeResult] = None)
