package db.models

import db.enums.OrderStatus
import org.jetbrains.exposed.dao.id.IntIdTable

object Orders : IntIdTable() {
    val user = reference("user", Users)
    val status = enumeration<OrderStatus>("status")
    val cookingTime = integer("processingTime")
}