package nl.dekkr.feedfrenzy.model

/**
 * Author: matthijs
 * Created on: 30 Jan 2015.
 */
case class ScrapeResult(
  block: Option[String] = None,
  articleUid: Option[String] = None,
  articleUrl: Option[String] = None,
  articleContent: Option[String] = None,
  articleTitle: Option[String] = None,
  articleAuthor: Option[String] = None)

