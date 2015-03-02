package nl.dekkr.feedfrenzy.model

import org.joda.time.DateTime

/**
 * RSS/Atom feed definition
 */
case class Feed(id: Option[Int] = None, feedurl: String, link: Option[String] = None, title: Option[String] = None, description: Option[String] = None, copyright: Option[String] = None, image: Option[String] = None, publisheddate: Option[DateTime] = None, updateddate: DateTime = DateTime.now(), updateInterval: Int = 60, nextupdate: Long = DateTime.now().getMillis, lastarticlecount: Int = 0, faviconfk: Int = 0, scraperId: Option[Int] = None)
