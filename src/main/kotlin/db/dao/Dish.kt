package org.example.db.dao

import org.example.db.models.Dishes
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Dish(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Dish>(Dishes)

    var name by Dishes.name
    var stock by Dishes.stock
    var cookingTime by Dishes.cookingTime
    var price by Dishes.price

    fun format() = "$name - $price * $stock. Время приготовления - $cookingTime мин"
}