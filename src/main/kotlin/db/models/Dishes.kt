package org.example.db.models

import org.jetbrains.exposed.dao.id.IntIdTable

object Dishes : IntIdTable() {
    val name = varchar("name", 255)
    val stock = integer("stock")
    val cookingTime = integer("cookingTime")
    val price = double("price")
}