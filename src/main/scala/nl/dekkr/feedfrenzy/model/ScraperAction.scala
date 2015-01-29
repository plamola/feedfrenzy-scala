package nl.dekkr.feedfrenzy.model

import nl.dekkr.feedfrenzy.model.ActionPhase.ActionPhase
import nl.dekkr.feedfrenzy.model.ActionType.ActionType

/**
 * Author: matthijs
 * Created on: 27 Jan 2015.
 */

object ActionType extends Enumeration {
  import scala.slick.driver.JdbcDriver.simple._

  type ActionType = Value
  val CSS_SELECTOR, ATTRIBUTE, CSS_SELECTOR_REMOVE, REGEX, TEMPLATE, REPLACE, CSS_SELECTOR_PARENT = Value

  val actionTypeMap = Map(
    ActionType.CSS_SELECTOR -> 0,
    ActionType.ATTRIBUTE -> 1,
    ActionType.CSS_SELECTOR_REMOVE -> 2,
    ActionType.REGEX -> 3,
    ActionType.TEMPLATE -> 4,
    ActionType.REPLACE -> 5,
    ActionType.CSS_SELECTOR_PARENT -> 6
  )

  implicit val actionTypeColumnTypeMapper = MappedColumnType.base[ActionType, Int](
    actionTypeMap,
    actionTypeMap.map(_.swap)
  )

}

object ActionPhase extends Enumeration {
  import scala.slick.driver.JdbcDriver.simple._

  type ActionPhase = Value
  val INDEX, UIDS, CONTENT = Value

  val actionPhaseMap = Map(
    ActionPhase.INDEX -> 0,
    ActionPhase.UIDS -> 1,
    ActionPhase.CONTENT -> 2
  )

  implicit val actionPhaseColumnTypeMapper = MappedColumnType.base[ActionPhase, Int](
    actionPhaseMap,
    actionPhaseMap.map(_.swap)
  )

}

case class ScraperAction(
  action_id: Option[Int] = None,
  scraper_id: Option[Int] = None,
  actionPhase: ActionPhase = ActionPhase.CONTENT,
  action_order: Int = 1,
  actionType: ActionType = ActionType.CSS_SELECTOR,
  actionInput: Option[String] = None,
  actionTemplate: Option[String] = None,
  actionOutputVariable: Option[String] = None,
  actionReplaceWith: Option[String] = None)

