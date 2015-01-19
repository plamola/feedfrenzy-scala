package nl.dekkr.feedfrenzy.streams

import java.util.concurrent.TimeUnit

import akka.actor.ActorLogging
import akka.event.Logging
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }
import nl.dekkr.feedfrenzy.model.{ Syndication, Scraper }
import scala.util.{ Failure, Success, Try }

/**
 * Created by Matthijs Dekker on 14/01/15.
 */
case class NewJobListing()

class ScraperActorPublisher extends ActorPublisher[Scraper] with ActorLogging {

  import scala.concurrent.duration._

  implicit val ec = context.dispatcher
  //val getNewJobs = context.system.scheduler.schedule(1 second, 1 second, self, NewJobListing)

  val getNewJobs = context.system.scheduler.schedule(
    Duration.create(0, TimeUnit.MILLISECONDS),
    Duration.create(1, TimeUnit.MINUTES),
    self, NewJobListing)

  var reloadScrapers = true
  var scrapers = List.empty[Scraper]

  def receive = {
    case NewJobListing() =>
      log.info("Received NewJobListing message")
      reloadScrapers = true

    case ActorPublisherMessage.Request(n) =>
      while (isActive && totalDemand > 0) {
        generateElement() match {
          case Success(valueOpt) =>
            valueOpt
              .map(element => onNext(element))
              .getOrElse(onComplete())
          case Failure(ex) =>
            log.info("No more jobs")
            // onError(ex)
            onComplete()
        }
      }
    case ActorPublisherMessage.Cancel =>
      cleanupResources()
    case ActorPublisherMessage.SubscriptionTimeoutExceeded =>
      cleanupResources()

  }

  override def preStart() = {
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
      scrapers = Syndication.getRunnableScrapers
    }
  }

}