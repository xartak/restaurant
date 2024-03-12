package org.example.db.dao

import db.dao.User
import db.models.Orders
import db.models.Reviews
import db.models.Users
import org.example.db.dao.Order
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Review(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Review>(Reviews)

    var order by Order referencedOn Reviews.order
    var user by User referencedOn Reviews.user
    var mark by Reviews.mark
    var comment by Reviews.comment
}