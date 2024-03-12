package db.models

import db.enums.UserType
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val username = varchar("username", 255)
    val password = varchar("password", 255)
    val type = enumeration<UserType>("type")
}