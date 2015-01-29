package nl.dekkr.feedfrenzy.model

import nl.dekkr.feedfrenzy.model.ActionPhase.ActionPhase
import nl.dekkr.feedfrenzy.model.ActionType.ActionType

/**
 * Author: matthijs
 * Created on: 27 Jan 2015.
 */

object ActionType extends Enumeration {
  type ActionType = Value
  val CSS_SELECTOR, ATTRIBUTE, CSS_SELECTOR_REMOVE, REGEX, TEMPLATE, REPLACE, CSS_SELECTOR_PARENT = Value
}

object ActionPhase extends Enumeration {
  type ActionPhase = Value
  val INDEX, UIDS, CONTENT = Value
}

case class ScraperAction(
  action_id: Option[Int],
  scraper_id: Option[Int],
  //actionPhase: ActionPhase = ActionPhase.CONTENT,
  action_order: Int = 1,
  // actionType: ActionType = ActionType.CSS_SELECTOR,
  actionInput: Option[String],
  actionTemplate: Option[String],
  actionReplaceWith: Option[String],
  actionOutputVariable: Option[String])

