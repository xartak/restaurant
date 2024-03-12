package db.models

import org.jetbrains.exposed.dao.id.IntIdTable

object Reviews : IntIdTable() {
    val user = reference("user", Users)
    val order = reference("order", Orders)
    val mark = integer("mark")
    val comment = text("comment")
}