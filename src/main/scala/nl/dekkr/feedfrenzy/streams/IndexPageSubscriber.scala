package nl.dekkr.feedfrenzy.streams

import akka.event.Logging
import akka.stream.actor.{ ActorSubscriberMessage, WatermarkRequestStrategy, ActorSubscriber }
import nl.dekkr.feedfrenzy.model.IndexPage

/**
 * Created by Matthijs Dekker on 14/01/15.
 */
class IndexPageSubscriber extends ActorSubscriber {

  val log = Logging(context.system, this)

  protected def requestStrategy = WatermarkRequestStrategy(10)

  def processElement(el: IndexPage) = log.info(s"Sink contentSink - Scraper [${el.scraper.id.getOrElse(0)}] -  Content length: ${el.content.getOrElse("").length}")

  def handleError(ex: Throwable) = log.error(ex.getMessage)

  def streamFinished() = log.info("Done")

  def receive = {
    case ActorSubscriberMessage.OnNext(element) =>
      processElement(element.asInstanceOf[IndexPage])
    case ActorSubscriberMessage.OnError(ex) =>
      handleError(ex)
    case ActorSubscriberMessage.OnComplete =>
      streamFinished()
  }
}