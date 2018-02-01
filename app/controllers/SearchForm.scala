package controllers

import java.time.{LocalDate, LocalDateTime}

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._


object SearchForm {

  case class Item(item: String, realName: String, timestamp: LocalDateTime, value: String)

  case class SearchData(itemName: String, from: LocalDate ,to: LocalDate)

  val searchFormConstraints = Form(
    mapping(
      "itemName" -> nonEmptyText,
      "from" -> of[LocalDate].as(localDateFormat("MM/dd/yyyy")),
      "to" -> of[LocalDate].as(localDateFormat("MM/dd/yyyy"))
    )(SearchData.apply)(SearchData.unapply)
  )

}