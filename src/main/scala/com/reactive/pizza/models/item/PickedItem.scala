package com.reactive.pizza.models.item

import com.reactive.pizza.models.item.Description.Size
import com.reactive.pizza.utils.UnExpectedItemException
import play.api.libs.json.{ Json, JsValue, Writes }

case class PickedItem(item: Item, quantity: Int, size: Option[Size]) {
  //---------------[ Validations ]------------------
  require(quantity > 0, "Item quantity must be greater than 0")

  //----------------[ Methods ]------------------------
  def getPrice: Int = {
    item match {
      case si: SizableItem    =>
        si.descr.sizeInfo.find(sz => size.contains(sz.size))
                         .map(_.price * quantity)
                         .getOrElse(throw itemWrong)
      case ns: NoSizeableItem =>
        ns.price * quantity
      case cb: ComboItem      =>
        cb.price * quantity
      case _                  =>
        throw new UnExpectedItemException("Item type is invalid")
    }
  }

  def getLabel: String = {
    item match {
      case si: SizableItem => s"${si.name} ${size.map(_.v).getOrElse(throw itemWrong)}"
      case that: Item      => s"${that.name} ${that.group.v}"
    }
  }

  //--------------[ Exceptions ]--------------------------
  private lazy val itemWrong = new UnExpectedItemException(s"Not found valid size for Item: ${item.id.v}")
}

object PickedItem {
  //----------[ Json converter ]-------------------
  implicit val pikItemWriter: Writes[PickedItem] = (pi: PickedItem) => {
    Json.obj(
      "itemId" -> pi.item.id.v,
      "quantity" -> pi.quantity,
      "size"     -> pi.size.map(_.v)
    )
  }

  //--------------[ Methods ]-----------------------
  def apply(json: JsValue, item: Item): PickedItem = {
    val quantity = (json \ "quantity").as[Int]
    val size     = (json \ "size").asOpt[String].map(Size.apply)

    PickedItem(item, quantity, size)
  }
}
