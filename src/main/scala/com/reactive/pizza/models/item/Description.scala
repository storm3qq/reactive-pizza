package com.reactive.pizza.models.item

import com.reactive.pizza.models.item.Description.SizeInfo
import com.reactive.pizza.utils.Message

class Description(ingredients: Seq[String]) {
  val extraText: Option[String] = Some(Message.ExtraText)

  //----------[ Validations ]------------
  require(
    ingredients.forall(v => 1 <= v.length && v.length <= 255),
    "Contain invalid ingredient"
  )
}

case class SizeDescription(ingredients: Seq[String], sizeInfo: Seq[SizeInfo]) extends Description(ingredients)

object Description {
  sealed abstract class Size(val v: String)
  final case object S extends Size("S")
  final case object M extends Size("M")
  final case object L extends Size("L")
  object Size {
    def apply(v: String): Size = v match {
      case "S" => S
      case "M" => M
      case "L" => L

      case _   => throw new IllegalArgumentException(s"Illegal size value: $v")
    }
  }
  //-----------------//-------------------
  case class SizeInfo(size: Size, cm: Int, price: Int) {
    //------------[ Validations ]-------------
    require(0 < cm,    "Cm must be greater than 0")
    require(0 < price, "Price must be greater than 0")
  }
}