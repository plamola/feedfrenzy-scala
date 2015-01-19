package nl.dekkr.feedfrenzy.streams.sinks

import akka.actor.ActorLogging
import akka.stream.actor.{ ActorSubscriber, ActorSubscriberMessage, WatermarkRequestStrategy }
import nl.dekkr.feedfrenzy.model.IndexPage

/**
 * Created by Matthijs Dekker on 14/01/15.
 */
class IndexPageSubscriber extends ActorSubscriber with ActorLogging {

  protected def requestStrategy = WatermarkRequestStrategy(10)

  def processElement(el: IndexPage) = log.info(s"IndexPageSubscriber: [${el.scraper.id.getOrElse(0)}] -  Content length: ${el.content.getOrElse("").length}")

  def handleError(ex: Throwable) = log.error(ex.getMessage)

  def streamFinished() = {
    log.info("Done")
  }

  def receive = {
    case ActorSubscriberMessage.OnNext(element) => {
      //TODO remove this delay
      Thread.sleep(2000)
      processElement(element.asInstanceOf[IndexPage])
    }
    case ActorSubscriberMessage.OnError(ex) =>
      handleError(ex)
    case ActorSubscriberMessage.OnComplete =>
      streamFinished()
  }
}