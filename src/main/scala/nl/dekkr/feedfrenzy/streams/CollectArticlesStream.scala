package nl.dekkr.feedfrenzy.streams

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl._
import nl.dekkr.feedfrenzy.model._
import nl.dekkr.feedfrenzy.streams.flows.{ GetPageContent, SplitIndexIntoBlocks }

/**
 * Created by Matthijs Dekker on 26/01/15.
 */
object CollectArticlesStream {

  def runWithSource(src: Source[ScraperDefinition]): Unit = {
    implicit val system = ActorSystem("feedfrenzy-collector")
    implicit val materializer = FlowMaterializer()

    println("########################################################################################################################")

    //val publisher: Publisher[ContentBlock] = ActorPublisher[ContentBlock](system.actorOf(Props[ScraperActorPublisher], "JobSource"))
    //val subscriber: Subscriber[ContentBlock] = ActorSubscriber[ContentBlock](system.actorOf(Props[IndexPageSubscriber], "IndexPageSubscriber"))

    // val core = new ScraperCore(new ScraperRepositoryDummyComponent)

    val scraperToContentBlock: Flow[ScraperDefinition, ContentBlock] = Flow[ScraperDefinition]
      .map(scraperDef => new ContentBlock(scraperDefinition = scraperDef, uri = Some(scraperDef.scraper.sourceUrl)))

    val fetchPage: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .transform(() => new GetPageContent())

    val splitIntoBlocks: Flow[ContentBlock, ContentBlock] = Flow[ContentBlock]
      .transform(() => new SplitIndexIntoBlocks())

    val printSink = ForeachSink[ContentBlock] {
      value =>
        println(s"PrintSink: [${value.scraperDefinition.scraper.id.get}] [${value.content.get.length}]")
        for { actions <- value.scraperDefinition.actions } println(s"[${actions.action_id}] - ${actions.action_order} ${actions.actionType} ${actions.actionTemplate}")
    }

    val resultSink = ForeachSink[ContentBlock] {
      el =>
        println(s"ResultSink: [${el.scraperDefinition.scraper.id.getOrElse(0)}] -  Content length: ${el.content.getOrElse("").length}")
    }

    val blockSink = ForeachSink[ContentBlock] {
      value =>
        println(s"BlockSink: [${value.scraperDefinition.scraper.id.get}] [${value.content.get}]")
    }

    val materialized = FlowGraph {
      implicit b =>
        import akka.stream.scaladsl.FlowGraphImplicits._
        val broadcast = Broadcast[ContentBlock]
        //        Source(publisher) ~> fetchPage ~> broadcast ~> Sink(subscriber)

        // @formatter:off
        src ~> scraperToContentBlock ~> fetchPage ~> broadcast ~> resultSink
        broadcast ~> splitIntoBlocks ~> blockSink //Sink.ignore
        broadcast ~> printSink
      // @formatter:on

    }.run

    //        import system.dispatcher
    //        materialized.get(resultSink).onComplete {
    //          case Success(_) =>
    //            //Try(output.close())
    //            system.shutdown()
    //          case Failure(e) =>
    //            println(s"Failure: ${e.getMessage}")
    //            //Try(output.close())
    //            system.shutdown()
    //        }
  }

}
