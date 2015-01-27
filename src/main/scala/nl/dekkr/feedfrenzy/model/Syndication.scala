package nl.dekkr.feedfrenzy.model

import nl.dekkr.feedfrenzy.db.{ Schema, Tables }
import org.joda.time.DateTime

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery

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

  def addNewFeed(url: String): Feed = {
    if (feeds.filter(_.feedurl === url).list.size == 0)
      feeds += Feed(feedurl = url)
    getFeed(url).get
  }

  def getFeed(url: String): Option[Feed] = feeds.filter(_.feedurl === url).firstOption

  def getFeedById(id: Int): Option[Feed] = feeds.filter(_.id === id).firstOption

  def removeFeed(url: String): Int = {
    feeds.filter(_.feedurl === url).delete
  }

}
