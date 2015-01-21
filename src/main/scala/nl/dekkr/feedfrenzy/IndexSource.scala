package nl.dekkr.feedfrenzy

import akka.actor.Status.{ Failure, Success }
import akka.actor.{ ActorSystem, Props }
import akka.stream.FlowMaterializer
import akka.stream.actor.{ ActorSubscriber, ActorPublisher }
import akka.stream.scaladsl._
import nl.dekkr.feedfrenzy.model.{ Scraper, Syndication, ContentBlock }
import nl.dekkr.feedfrenzy.streams.flows.{ GetPageContent, SplitIndexIntoBlocks }
import nl.dekkr.feedfrenzy.streams.sinks.IndexPageSubscriber
import nl.dekkr.feedfrenzy.streams.sources.ScraperActorPublisher
import org.reactivestreams.{ Publisher, Subscriber }

/**
 * Author: Matthijs
 * Created on: 11 Jan 2015.
 */

object IndexSource {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("feedfrenzy-collector")
    implicit val materializer = FlowMaterializer()

    println("########################################################################################################################")

    //val publisher: Publisher[ContentBlock] = ActorPublisher[ContentBlock](system.actorOf(Props[ScraperActorPublisher], "JobSource"))
    //val subscriber: Subscriber[ContentBlock] = ActorSubscriber[ContentBlock](system.actorOf(Props[IndexPageSubscriber], "IndexPageSubscriber"))

    val src: Source[Scraper] = Source(Syndication.getRunnableScrapers)

    val scraperToContentBlock: Flow[Scraper, ContentBlock] = Flow[Scraper]
      .map(scraper =>
        new ContentBlock(scraper = scraper, uri = Some(scraper.sourceUrl))
      )

    val fetchPage: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .transform(() => new GetPageContent())

    val splitIntoBlocks: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .transform(() => new SplitIndexIntoBlocks())

    val printSink = ForeachSink[ContentBlock] {
      value =>
        println(s"PrintSink: [${value.scraper.id.get}] [${value.content.get.length}]")
    }

    val resultSink = ForeachSink[ContentBlock] {
      el =>
        println(s"ResultSink: [${el.scraper.id.getOrElse(0)}] -  Content length: ${el.content.getOrElse("").length}")
    }

    val materialized = FlowGraph {
      implicit b =>
        import FlowGraphImplicits._
        val broadcast = Broadcast[ContentBlock]
        //        Source(publisher) ~> fetchPage ~> broadcast ~> Sink(subscriber)
        src ~> scraperToContentBlock ~> fetchPage ~> broadcast ~> resultSink
        broadcast ~> splitIntoBlocks ~> Sink.ignore
    }.run

    //    import system.dispatcher
    //    materialized.get(sink).onComplete {
    //      case Success(_) =>
    //        //Try(output.close())
    //        system.shutdown()
    //      case Failure(e) =>
    //        println(s"Failure: ${e.getMessage}")
    //        //Try(output.close())
    //        system.shutdown()
    //    }

  }

}
