package nl.dekkr.feedfrenzy.model

import nl.dekkr.feedfrenzy.db.{ Schema, Tables }
import org.joda.time.DateTime

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery

case class Scraper(id: Option[Int] = None, sourceUrl: String, singlePage: Boolean = false)

case class IndexPage(scraper: Scraper, content: Option[String] = None)

case class IndexBlock(id: Option[Int] = None, indexpageid: Option[Int] = None, sourceurl: String, updateddate: DateTime = DateTime.now(), content: Option[String] = None)

case class Feed(id: Option[Int] = None, feedurl: String, link: Option[String] = None, title: Option[String] = None, description: Option[String] = None, copyright: Option[String] = None, image: Option[String] = None, publisheddate: Option[DateTime] = None, updateddate: DateTime = DateTime.now(), updateInterval: Int = 60, nextupdate: Long = DateTime.now().getMillis, lastarticlecount: Int = 0, faviconfk: Int = 0)

case class Article(id: Option[Int] = None, feedid: Option[Int] = None, uri: String, link: Option[String] = None, title: Option[String] = None, content: Option[String] = None, author: Option[String] = None, publisheddate: Option[DateTime] = None, updateddate: Option[DateTime] = None, lastsynceddate: Option[DateTime] = None)

/**
 * Handles syndication processing
 */
object Syndication {

  implicit val session = Schema.getSession
  val scrapers = TableQuery[Tables.ScraperTable]
  val feeds = TableQuery[Tables.FeedTable]
  val articles = TableQuery[Tables.ArticleTable]

  def getFeedsForUpdate: List[String] = {
    for { c <- feeds.list if c.nextupdate < DateTime.now().getMillis } yield c.feedurl
  }

  def setNextUpdate(uri: String): Unit = {
    val query = feeds.filter(_.feedurl === uri)
    val feed = query.first
    val updatedFeed = feed.copy(
      nextupdate = DateTime.now().plusMinutes(feed.updateInterval).getMillis
    )
    query.update(updatedFeed)
  }

  def getFeed(url: String): Option[Feed] = feeds.filter(_.feedurl === url).firstOption

  def getFeedById(id: Int): Option[Feed] = feeds.filter(_.id === id).firstOption

  def removeFeed(url: String): Int = {
    feeds.filter(_.feedurl === url).delete
  }

}
