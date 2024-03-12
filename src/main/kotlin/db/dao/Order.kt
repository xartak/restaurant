package org.example.db.dao

import db.dao.User
import db.models.Orders
import org.example.db.models.OrderDishes
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Order(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Order>(Orders)

    var user by User referencedOn Orders.user
    var status by Orders.status
    var dishes by Dish via OrderDishes
    var cookingTime by Orders.cookingTime

    fun format() = "${id.value} - ${status.title}, ${dishes.sumOf { it.price }} руб. До приготовления $cookingTime мин."
}