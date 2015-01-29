package nl.dekkr.feedfrenzy.model

/**
 * Author: matthijs
 * Created on: 29 Jan 2015.
 */
case class ScraperDefinition(
  scraper: Scraper,
  actions: List[ScraperAction] = List.empty)

