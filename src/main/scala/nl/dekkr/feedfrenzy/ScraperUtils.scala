package nl.dekkr.feedfrenzy

import java.util.regex.{ Matcher, Pattern }

import nl.dekkr.feedfrenzy.model.ActionPhase.ActionPhase
import nl.dekkr.feedfrenzy.model.{ ActionType, ScraperAction }
import org.jsoup.Jsoup

/**
 * Author: matthijs
 * Created on: 29 Jan 2015.
 */
object ScraperUtils {

  import scala.collection.JavaConversions._

  //TODO Methods are converted from Java / Clean them up

  def getIDs(content: String, selector: String): List[String] = {
    try {
      val doc = Jsoup.parse(content)
      val blockList = doc.select(selector).iterator.toList
      for { element <- blockList } yield element.parent().html()
    } catch {
      case e: Exception =>
        List.empty[String]
    }
  }

  private def replaceVarsInTemplate(template: String, vars: Map[String, String]): String = {
    var output = template
    vars.foreach(
      item =>
        output = output.replaceAll("{" + item._1 + "}", item._2)
    )
    output
  }

  def performScraperActions(actions: List[ScraperAction], phase: ActionPhase, vars: Map[String, String]): Map[String, String] = {
    for (action <- actions) {
      val input: String = vars.get(action.actionInput)
      val template: String = action.actionTemplate.get
      val replaceWith: String = action.actionReplaceWith.get
      var output: String = "-- Error? --"

      action.actionType match {
        case ActionType.REGEX =>
          try {
            val p: Pattern = Pattern.compile(template)
            val m: Matcher = p.matcher(input)
            if (m.find) {
              output = m.group(1)
            } else {
              output = ""
            }
          } catch {
            case e: Exception => {
              output = e.getMessage
            }
          }

        case ActionType.CSS_SELECTOR =>
        case ActionType.CSS_SELECTOR_PARENT =>
          if (input != null && input.length > 0) {
            val doc = Jsoup.parse(input)
            val updatedTemplate: String = replaceVarsInTemplate(template, vars)
            if (updatedTemplate.isEmpty) {
              println("Empty string - scraperId: " + action.scraper_id.get + "phase: " + phase + " template " + template)
              output = ""
            } else {
              val contentList = doc.select(updatedTemplate)
              if (contentList != null) {
                if (action.actionType == ActionType.CSS_SELECTOR_PARENT) {
                  output = contentList.outerHtml
                } else {
                  output = contentList.html
                }
              } else {
                output = ""
              }
            }
          } else {
            output = ""
          }

        case ActionType.CSS_SELECTOR_REMOVE =>
          if (input != null && input.length > 0) {
            val doc = Jsoup.parse(input)
            val updatedTemplate: String = replaceVarsInTemplate(template, vars)
            val removeThisContent = doc.select(updatedTemplate).first
            if (removeThisContent != null) {
              removeThisContent.remove
              output = doc.body.html
            } else {
              output = input
            }
          } else {
            output = ""
          }

        case ActionType.ATTRIBUTE =>
          // val elm: Nothing = new Nothing("")
          //elm.html(input)
          val elm = Jsoup.parse(input)
          try {
            output = elm.child(0).attr(template)
          } catch {
            case e: Exception => {
              output = ""
            }
          }

        case ActionType.TEMPLATE =>
          output = replaceVarsInTemplate(template, vars)

        case ActionType.REPLACE =>
          val find: String = replaceVarsInTemplate(template, vars)
          val replace: String = replaceVarsInTemplate(replaceWith, vars)
          output = input.replaceAll(find, replace)

        case _ =>
          output = "-- actionType not implemented --"

      }
      if (vars.containsKey(action.actionOutputVariable)) {
        vars.remove(action.actionOutputVariable)
      }
      vars.put(action.actionOutputVariable.get, output.trim)
    }
    vars
  }

}