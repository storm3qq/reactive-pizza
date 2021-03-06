package com.reactive.pizza.repositories.persistences.tables

import com.reactive.pizza.models.cart.Cart
import com.reactive.pizza.models.coupon.Coupon
import com.reactive.pizza.models.item.{ Item, PickedItem }
import com.reactive.pizza.models.user.User
import com.reactive.pizza.repositories.persistences.{ ColumnCustomType, MySqlDBComponent }
import org.joda.time.DateTime
import play.api.libs.json.{ Json, JsValue }

import javax.inject._

@Singleton
class CartDAO @Inject()(dbComponent: MySqlDBComponent, itemDAO: ItemDAO) {
  import dbComponent.mysqlDriver.api._

  type CartTupled = (Cart.Id, String, Option[Coupon.Id], User.Id, DateTime, DateTime)

  private[CartDAO] class CartTable(tag: Tag) extends Table[CartTupled](tag, "cart") with ColumnCustomType {
    //----------[ Columns ]----------------------
    def id: Rep[Cart.Id]                 = column[Cart.Id]("id", O.PrimaryKey)
    def itemIds: Rep[String]             = column[String]("item")
    def couponId: Rep[Option[Coupon.Id]] = column[Option[Coupon.Id]]("coupon_id")
    def userId: Rep[User.Id]             = column[User.Id]("user_id")
    def createdAt: Rep[DateTime]         = column[DateTime]("created_at")
    def updatedAt: Rep[DateTime]         = column[DateTime]("updated_at")

    def * = (id, itemIds, couponId, userId, createdAt, updatedAt)
  }

  val carts = TableQuery[CartTable]
  //-------------[ Methods ]----------------------------
  def apply(c: CartTupled, couponOpt: Option[Coupon], itemMap: Map[Item.Id, Item]): Cart = {
    val pikItemsJson = Json.parse(c._2).as[Seq[JsValue]]
    val pikItems     = pikItemsJson.map { json =>
      val idStr  = (json \ "itemId").as[String]
      val itemId = Item.Id(idStr)
      val item   = itemMap.getOrElse(itemId, throw itemDAO.notFound(itemId))

      PickedItem(json, item)
    }

    Cart(c._1, pikItems, couponOpt, c._4, c._5, c._6)
  }

  def unapply(c: Cart): CartTupled = {
    (c.id, Json.toJson(c.pikItems).toString(), c.coupon.map(_.id), c.userId, c.createdAt, c.updatedAt)
  }
}
