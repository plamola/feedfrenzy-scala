package nl.dekkr.feedfrenzy.model

import org.joda.time.DateTime

/**
 * Entity for caching a page
 */
case class PageCache(id: Option[Int] = None, uri: String, content: Option[String] = None, createdAt : DateTime )
