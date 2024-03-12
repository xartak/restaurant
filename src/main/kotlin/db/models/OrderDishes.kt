package org.example.db.models

import db.models.Orders
import org.jetbrains.exposed.sql.Table

object OrderDishes : Table() {
    private val order = reference("order", Orders)
    private val dish = reference("dish", Dishes)

    override val primaryKey = PrimaryKey(order, dish)
}