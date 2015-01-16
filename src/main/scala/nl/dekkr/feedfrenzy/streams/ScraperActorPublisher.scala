package nl.dekkr.feedfrenzy.streams

import akka.event.Logging
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }
import nl.dekkr.feedfrenzy.db.{ Schema, Tables }
import nl.dekkr.feedfrenzy.model.{ Feed, Scraper }

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.TableQuery
import scala.util.{ Failure, Success, Try }

/**
 * Created by Matthijs Dekker on 14/01/15.
 */
case class NewJobListing()

class ScraperActorPublisher extends ActorPublisher[Scraper] {

  import scala.concurrent.duration._

  implicit val ec = context.dispatcher

  val getNewJobs = context.system.scheduler.schedule(1 second, 1 minute, self, NewJobListing)
  val log = Logging(context.system, this)

  var scrapers = List.empty[Scraper]
  var reloadScrapers = true

  def receive = {
    case ActorPublisherMessage.Request(n) =>
      while (isActive && totalDemand > 0) {
        generateElement() match {
          case Success(valueOpt) =>
            valueOpt
              .map(element => onNext(element))
              .getOrElse(onComplete())
          case Failure(ex) =>
            onError(ex)
        }
      }
    case ActorPublisherMessage.Cancel =>
      cleanupResources()
    case ActorPublisherMessage.SubscriptionTimeoutExceeded =>
      cleanupResources()
    case NewJobListing() => {
      log.info("Received NewJobListing message")
      reloadScrapers = true
    }

  }

  override def preStart(): Unit = {
    createResources()
  }

  override def postStop() = {
    log.info("Stopping ScraperActorPublisher")
    getNewJobs.cancel()
  }

  def generateElement(): Try[Option[Scraper]] = {
    if (scrapers.size == 0 && reloadScrapers) {
      reloadScrapers = false
      createResources()
    }
    if (scrapers.size == 0) {
      log.info("No more jobs")
      Failure(new Exception("No more jobs"))
    } else {
      val element = scrapers.head
      scrapers = scrapers.tail
      Success(Some(element))
    }
  }

  def cleanupResources() = {
    log.info("Cleanup resources")
    scrapers = List.empty[Scraper]
  }

  def createResources() = {
    if (scrapers.size == 0 && reloadScrapers) {

      log.info("Getting new resources")
      reloadScrapers = false
      scrapers = getUpdatableFeeds.map(feed => Scraper(id = feed.id, sourceUrl = feed.feedurl, singlePage = true))
    }
  }

  def getUpdatableFeeds: List[Feed] = {
    try {
      val feeds = TableQuery[Tables.FeedTable]
      implicit val session = Schema.getSession
      Schema.createOrUpdate(session)

      // Add some dummy feeds
      if (feeds.list.size < 1)
        feeds += Feed(feedurl = "http://nu.nl")

      if (feeds.list.size < 2)
        feeds += Feed(feedurl = "http://dagartikel.nl")

      if (feeds.list.size < 3)
        feeds += Feed(feedurl = "http://news.google.com")

      if (feeds.list.size < 4)
        feeds += Feed(feedurl = "http://news.yahoo.com")

      if (feeds.list.size < 5)
        feeds += Feed(feedurl = "http://www.theverge.com/")

      if (feeds.list.size < 6)
        feeds += Feed(feedurl = "http://news.ycombinator.com/")

      feeds.list
    } catch {
      case e: Exception =>
        println(s"ERROR: ${e.getMessage} [${e.getCause}]")
        List.empty
    }
  }

}