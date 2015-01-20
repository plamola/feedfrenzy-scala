package nl.dekkr.feedfrenzy.streams.flows

import akka.stream.stage.{ Context, Directive, PushPullStage, TerminationDirective }
import nl.dekkr.feedfrenzy.model.{ ContentBlock, IndexPage }

/**
 * Author: matthijs
 * Created on: 19 Jan 2015.
 */
class SplitIndexIntoBlocks() extends PushPullStage[IndexPage, ContentBlock] {
  private var blocks: List[String] = List.empty[String]
  private var content: IndexPage = _

  override def onPush(elem: IndexPage, ctx: Context[ContentBlock]): Directive = {
    content = elem
    // TODO Split should be scraper dependent (current implementation is for demo)
    blocks = elem.content.getOrElse("").split("<div").toList
    ctx.push(getNextBlock)
  }

  override def onPull(ctx: Context[ContentBlock]): Directive = {
    if (!ctx.isFinishing) {
      if (blocks.length > 0) {
        ctx.push(getNextBlock)
      } else
        ctx.pull()
    } else {
      // Make sure all blocks are sent out
      if (blocks.length == 0)
        ctx.finish()
      else if (blocks.length == 1)
        ctx.pushAndFinish(getNextBlock)
      else
        ctx.push(getNextBlock)
    }
  }
  override def onUpstreamFinish(ctx: Context[ContentBlock]): TerminationDirective =
    ctx.absorbTermination()

  def getNextBlock: ContentBlock = {
    val elem = blocks.head
    blocks = blocks.tail
    new ContentBlock(scraper = content.scraper, content = Option(elem))
  }

}
