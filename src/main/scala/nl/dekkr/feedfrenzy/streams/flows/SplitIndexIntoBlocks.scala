package nl.dekkr.feedfrenzy.streams.flows

import akka.stream.stage.{ Context, Directive, PushPullStage, TerminationDirective }
import nl.dekkr.feedfrenzy.ScraperUtils
import nl.dekkr.feedfrenzy.model.{ ActionPhase, ContentBlock }

/**
 * Author: matthijs
 * Created on: 19 Jan 2015.
 */
class SplitIndexIntoBlocks() extends PushPullStage[ContentBlock, ContentBlock] {
  private var blocks: List[String] = List.empty[String]
  private var content: ContentBlock = _

  override def onPush(elem: ContentBlock, ctx: Context[ContentBlock]): Directive = {
    content = elem
    // TODO Split should take all actions into account, not just the first one
    val splitOn = elem.scraperDefinition.actions.filter(_.actionPhase == ActionPhase.INDEX).head.actionTemplate
    blocks = ScraperUtils.getIDs(elem.content.get, splitOn.get)
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
    new ContentBlock(scraperDefinition = content.scraperDefinition, content = Option(elem))
  }

}
