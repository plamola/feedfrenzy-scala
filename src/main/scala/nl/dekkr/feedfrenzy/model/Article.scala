package nl.dekkr.feedfrenzy.model

import org.joda.time.DateTime

/**
 * Created by Matthijs Dekker on 26/01/15.
 */
case class Article(id: Option[Int] = None, feedid: Option[Int] = None, uri: String, link: Option[String] = None, title: Option[String] = None, content: Option[String] = None, author: Option[String] = None, publisheddate: Option[DateTime] = None, updateddate: Option[DateTime] = None, lastsynceddate: Option[DateTime] = None)
