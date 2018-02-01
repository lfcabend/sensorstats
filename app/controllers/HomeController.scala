package controllers

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}
import javax.inject._

import controllers.SearchForm.{Item, SearchData}
import play.api.mvc._
import play.modules.reactivemongo._
import play.api.mvc.{Action, Request}

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONHandler, Macros, document}
import play.api.i18n.Messages.Implicits
import play.api.i18n.{I18nSupport, MessagesApi}

import ExecutionContext.Implicits.global

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(val ac: DefaultActionBuilder,
                               val reactiveMongoApi: ReactiveMongoApi,
                               val messagesApi: MessagesApi,
                               val bp: PlayBodyParsers,
                               cc: ControllerComponents)
  extends MongoController with ReactiveMongoComponents with I18nSupport {

  def collection: Future[BSONCollection] = database.map(_.collection[BSONCollection]("openhab"))

  implicit object BSONDateHandler extends BSONHandler[BSONDateTime, LocalDate] {

    def read(time: BSONDateTime) = new Timestamp(time.value).toLocalDateTime.toLocalDate

    def write(date: LocalDate) = BSONDateTime(Timestamp.valueOf(date.atStartOfDay()).getTime)

  }

  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, LocalDateTime] {

    def read(time: BSONDateTime) = new Timestamp(time.value).toLocalDateTime

    def write(time: LocalDateTime) = BSONDateTime(Timestamp.valueOf(time).getTime)

  }

  implicit def personReader: BSONDocumentReader[Item] = Macros.reader[Item]

  implicit def personWriter: BSONDocumentWriter[Item] = Macros.writer[Item]

  val options = Seq(
    "Humidity" -> "Humidity",
    "Temperature" -> "Temperature",
    "door01" -> "door01"
  )

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = ac.async { implicit request =>
    val result = findItemSize("FrontDoor")
    result.map(x => Ok(views.html.index(String.valueOf(x))))
  }

  val search = ac.async { implicit request =>
    val formResult = SearchForm.searchFormConstraints.bindFromRequest
    formResult.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        Future(BadRequest(views.html.testboot(formWithErrors, options, postUrl, Nil)))
      },
      searchData => {
        /* binding success, you get the actual value. */
        val result = findItems(searchData)
        result.map(items => Ok(views.html.testboot(
          formResult, options, postUrl, items)))
      })
  }

  private val postUrl = routes.HomeController.search()

  def testboot() = ac { implicit request =>
    Ok(views.html.testboot(SearchForm.searchFormConstraints, options, postUrl, Nil))
  }

  def findItems(searchData: SearchData): Future[List[Item]] = {
    val fromTime = BSONDateHandler.write(searchData.from)
    val toTime = BSONDateHandler.write(searchData.to)
    val query = BSONDocument("item" -> searchData.itemName,
      BSONDocument("timestamp" -> BSONDocument("$gt" -> fromTime)),
      BSONDocument("timestamp" -> BSONDocument("$lt" -> toTime))
    )
    collection.flatMap(_.find(query). // query builder
      sort(BSONDocument("timestamp" -> -1)).
      cursor[Item]().collect[List](Int.MaxValue, Cursor.FailOnError[List[Item]]())) // collect using the result cursor
  }

  def findItemSize(name: String): Future[Int] = {
    collection.flatMap(_.find(document("item" -> name)). // query builder
      cursor[Item]().collect[List](Int.MaxValue, Cursor.FailOnError[List[Item]]()).map(_.size)) // collect using the result cursor
  }

}
