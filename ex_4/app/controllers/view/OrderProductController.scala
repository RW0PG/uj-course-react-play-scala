package controllers.view

import models.{Order, OrderProduct, Product}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import services.{OrderProductRepository, OrderRepository, ProductRepository}

import javax.inject._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class OrderProductController @Inject()(OrderProductRepository: OrderProductRepository, OrderRepository: OrderRepository, ProductRepository: ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val orderProductForm: Form[CreateOrderProductForm] = Form {
    mapping(
      "orderId" -> longNumber,
      "productId" -> longNumber,
      "amount" -> number,
    )(CreateOrderProductForm.apply)(CreateOrderProductForm.unapply)
  }

  val updateOrderProductForm: Form[UpdateOrderProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "orderId" -> longNumber,
      "productId" -> longNumber,
      "amount" -> number,
    )(UpdateOrderProductForm.apply)(UpdateOrderProductForm.unapply)
  }

  def fetchData(): Unit = {

    OrderRepository.list().onComplete {
      case Success(orders) => orderList = orders
      case Failure(e) => print("error while listing orders", e)
    }

    ProductRepository.list().onComplete {
      case Success(products) => productList = products
      case Failure(e) => print("error while listing products", e)
    }
  }

  var orderList: Seq[Order] = Seq[Order]()
  var productList: Seq[Product] = Seq[Product]()

  fetchData()

  def createOrderProductHandle(): Action[AnyContent] = Action.async { implicit request =>
    orderProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.order_product_create(errorForm, orderList, productList))
        )
      },
      orderProduct => {
        OrderProductRepository.create(orderProduct.orderId, orderProduct.productId, orderProduct.amount).map { _ =>
          Redirect("/form/order-product/list")
        }
      }
    )
  }

  def listOrderProducts: Action[AnyContent] = Action.async { implicit request =>
    OrderProductRepository.list().map(orderProducts => Ok(views.html.order_product_list(orderProducts)))
  }

  def createOrderProduct(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val orders = Await.result(OrderRepository.list(), 1.second)
    val products = ProductRepository.list()

    products.map(orderProducts => Ok(views.html.order_product_create(orderProductForm, orders, orderProducts)))
  }

  def updateOrderProduct(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val orderProduct = OrderProductRepository.getByIdOption(id)
    orderProduct.map(orderProduct => {
      val prodForm = updateOrderProductForm.fill(UpdateOrderProductForm(orderProduct.get.id, orderProduct.get.orderId, orderProduct.get.productId, orderProduct.get.amount))
      Ok(views.html.order_product_update(prodForm, orderList, productList))
    })
  }

  def updateOrderProductHandle(): Action[AnyContent] = Action.async { implicit request =>
    updateOrderProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.order_product_update(errorForm, orderList, productList))
        )
      },
      orderProduct => {
        OrderProductRepository.update(orderProduct.id, OrderProduct(orderProduct.id, orderProduct.orderId, orderProduct.productId, orderProduct.amount)).map { _ =>
          Redirect("/form/order-product/list")
        }
      }
    )
  }

  def deleteOrderProduct(id: Long): Action[AnyContent] = Action {
    OrderProductRepository.delete(id)
    Redirect("/form/order-product/list")
  }
}

case class CreateOrderProductForm(orderId: Long, productId: Long, amount: Int)

case class UpdateOrderProductForm(id: Long, orderId: Long, productId: Long, amount: Int)