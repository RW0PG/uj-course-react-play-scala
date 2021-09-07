package models

import play.api.libs.json.{Json, OFormat}

case class ProductCategory(id: Long, categoryName: String)

object ProductCategory{
  implicit val productCategoryFormat: OFormat[ProductCategory] = Json.format[ProductCategory]
}
