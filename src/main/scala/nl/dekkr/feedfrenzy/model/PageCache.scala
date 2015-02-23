package nl.dekkr.feedfrenzy.model

import org.joda.time.DateTime

/**
 * Created by Matthijs Dekker on 26/01/15.
 */
case class PageCache(id: Option[Int] = None, uri: String, content: Option[String] = None, createdAt : Option[DateTime] )
