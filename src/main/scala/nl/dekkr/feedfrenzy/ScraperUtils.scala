package nl.dekkr.feedfrenzy

import java.util.regex.{ Matcher, Pattern }

import nl.dekkr.feedfrenzy.model.ActionPhase.ActionPhase
import nl.dekkr.feedfrenzy.model._
import org.jsoup.Jsoup

/**
 * Author: matthijs
 * Created on: 29 Jan 2015.
 */
object ScraperUtils {

  import scala.collection.JavaConversions._

  def scrape(block: ContentBlock, actionPhase: ActionPhase): ContentBlock = {
    println(s"Scrape: \t $actionPhase \t ${block.pageContent.getOrElse("").length}")
    actionPhase match {
      case ActionPhase.INDEX =>
        // Not usable
        block

      case ActionPhase.UIDS =>
        val initialVariableMap: Map[String, String] = Map("block" -> block.result.get.block.get)
        val vars = ScraperUtils.performScraperActions(block.scraperDefinition.actions, actionPhase, initialVariableMap)
        if (vars.contains("feeditem_uid") && vars.contains("feeditem_url"))
          setContentBlockResult(block, Some(ScrapeResult(
            block = None,
            articleUid = vars.get("feeditem_uid"),
            articleUrl = vars.get("feeditem_url")
          )))
        else
          setContentBlockResult(block, None)

      case ActionPhase.CONTENT =>
        val initialVariableMap: Map[String, String] = Map(
          "contentBody" -> block.pageContent.get,
          "feeditem_uri" -> block.result.get.articleUrl.get,
          "feeditem_uid" -> block.result.get.articleUid.get
        )
        //        for (variable <- initialVariableMap) {
        //          println(s"${variable._1}  \t---\t  ${variable._2}")
        //        }

        val vars = ScraperUtils.performScraperActions(block.scraperDefinition.actions, actionPhase, initialVariableMap)
        for (variable <- vars) {
          if (variable._1 != "block" && variable._1 != "contentBody" && variable._1 != "content" && variable._1 != "feeditem_content")
            println(s"${variable._1}  \t---\t  ${variable._2}")
        }
        setContentBlockResult(block,
          Some(
            ScrapeResult(
              block = None,
              articleContent = vars.get("feeditem_content"),
              articleTitle = vars.get("feeditem_title"),
              articleAuthor = vars.get("feeditem_author"),
              articleUid = vars.get("feeditem_uid"),
              articleUrl = vars.get("feeditem_url")
            )
          )
        )

    }

  }

  def setContentBlockResult(block: ContentBlock, result: Option[ScrapeResult]): ContentBlock =
    ContentBlock(
      scraperDefinition = block.scraperDefinition,
      sourceUrl = block.sourceUrl,
      pageContent = block.pageContent,
      result = result
    )

  //TODO Methods are converted from Java / Clean them up

  def getIDs(content: String, selector: String): List[String] = {
    try {
      val blockList = Jsoup.parse(content).select(selector).iterator.toList
      for { element <- blockList } yield element.parent().html()
    } catch {
      case e: Exception =>
        List.empty[String]
    }
  }

  def performScraperActions(actions: List[ScraperAction], phase: ActionPhase, variables: Map[String, String]): Map[String, String] = {
    //println(s"Action list length: ${actions.length}")
    var vars = variables

    for (action <- actions.filter(a => a.actionPhase == phase)) {
      if (phase == ActionPhase.CONTENT) println(s"Current action: ${action.action_order} \t${action.actionType} \t ${action.actionTemplate.getOrElse("")}  \t ${action.actionOutputVariable.getOrElse("")}")

      val input: String = vars.get(action.actionInput.getOrElse("")).getOrElse("<-- No input found-->")
      val template: String = action.actionTemplate.getOrElse("")
      var output: String = "-- Error? --"

      action.actionType match {
        case ActionType.REGEX =>
          output = extractWithRegEx(input, template)

        case ActionType.CSS_SELECTOR =>
          output = extractWithCssSelector(input, template, vars, includeParentHtml = false)

        case ActionType.CSS_SELECTOR_PARENT =>
          output = extractWithCssSelector(input, template, vars, includeParentHtml = true)

        case ActionType.CSS_SELECTOR_REMOVE =>
          output = removeWithCssSelector(input, template, vars)

        case ActionType.ATTRIBUTE =>
          output = extractAttribute(input, template)

        case ActionType.TEMPLATE =>
          output = replaceVarsInTemplate(template, vars)
        //println(s"TEMPLATE [$template] [$output]")

        case ActionType.REPLACE =>
          val replaceWith: String = action.actionReplaceWith.getOrElse("")
          output = input.replaceAllLiterally(replaceVarsInTemplate(template, vars), replaceVarsInTemplate(replaceWith, vars))

        case _ =>
          // TODO log error
          output = "-- actionType not implemented --"

      }

      if (vars.containsKey(action.actionOutputVariable.getOrElse("------"))) {
        vars = vars - action.actionOutputVariable.getOrElse("------")
      }
      vars = vars + (action.actionOutputVariable.getOrElse("+++++++++") -> output.trim)

      //      for (variable <- vars) {
      //        println(s"${variable._1}  \t---\t  ${variable._2}")
      //      }
    }
    vars
  }

  private def removeWithCssSelector(input: String, template: String, vars: Map[String, String]): String = {
    if (input != null && input.length > 0) {
      val doc = Jsoup.parse(input)

      val removeThisContent = doc.select(replaceVarsInTemplate(template, vars)).first
      if (removeThisContent != null) {
        doc.body.html.replaceAllLiterally(removeThisContent.outerHtml(), "")
      } else {
        input
      }
    } else {
      ""
    }
  }

  private def extractWithCssSelector(input: String, template: String, vars: Map[String, String], includeParentHtml: Boolean): String = {
    if (input != null && input.length > 0) {
      val doc = Jsoup.parse(input)
      //val doc = Jsoup.parse("<html></html").html(input)
      val updatedTemplate: String = replaceVarsInTemplate(template, vars)
      if (updatedTemplate.isEmpty) {
        println(s"Empty string - $template")
        ""
      } else {
        val contentList = doc.select(updatedTemplate)
        if (contentList != null) {
          if (includeParentHtml) {
            contentList.outerHtml()
          } else {
            contentList.html()
          }
        } else {
          ""
        }
      }
    } else {
      ""
    }
  }

  private def extractWithRegEx(input: String, template: String): String = {
    try {
      val p: Pattern = Pattern.compile(template)
      val m: Matcher = p.matcher(input)
      if (m.find) {
        //println(s"REGEX: [$input] [$template] [${m.group(1)}]")
        m.group(1)
      } else {
        //println(s"REGEX: [$input] [$template] []")
        ""
      }
    } catch {
      case e: Exception =>
        // TODO log exception
        ""
    }

  }

  private def extractAttribute(input: String, attrib: String): String = {
    try {
      Jsoup.parse("<html></html").html(input).child(0).attr(attrib)
    } catch {
      case e: Exception =>
        // TODO log an error: Attibute not found or empty input
        ""
    }
  }

  private def replaceVarsInTemplate(template: String, vars: Map[String, String]): String = {
    // TODO make recursive
    var output = template
    for (item <- vars) {
      //println(s"$output ::: ${item._1} :::  ${item._2}")
      output = output.replaceAllLiterally(s"{${item._1}}", item._2)
    }
    output
  }

}