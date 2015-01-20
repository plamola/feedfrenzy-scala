package nl.dekkr.feedfrenzy.streams.sources

import akka.actor.ActorLogging
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }
import nl.dekkr.feedfrenzy.model.{ Scraper, Syndication }

import scala.util.{ Failure, Success, Try }

/**
 * Created by Matthijs Dekker on 14/01/15.
 */

class ScraperActorPublisher extends ActorPublisher[Scraper] with ActorLogging {

  //  implicit val ec = context.dispatcher

  var reloadScrapers = true
  var scrapers = List.empty[Scraper]

  def receive = {
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
    //    getNewJobs.cancel()
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