package db

import db.models.*
import db.dao.*
import db.enums.*
import org.example.db.dao.Dish
import org.example.db.dao.Order
import org.example.db.dao.Review
import org.example.db.models.Dishes
import org.example.db.models.OrderDishes
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import utils.md5
import java.sql.Connection

class Database {
    init {
        Database.connect("jdbc:sqlite:restaurant.db", driver = "org.sqlite.JDBC", databaseConfig = DatabaseConfig {
            keepLoadedReferencesOutOfTransaction = true
        })
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            SchemaUtils.create(Users, Orders, OrderDishes, Dishes, Reviews)
        }
    }

    fun getUser(username: String, password: String): User? {
        return transaction {
            User.find {
                (Users.username eq username).and(Users.password eq password.md5())
            }.firstOrNull()
        }
    }

    fun addUser(username: String, password: String, type: UserType): User {
        return transaction {
            User.new {
                this.username = username
                this.password = password
                this.type = type
            }
        }
    }

    fun usernameExists(username: String): Boolean {
        return !transaction {
            User.find { Users.username eq username }.empty()
        }
    }

    fun addDish(name: String, price: Double, stock: Int, cookingTime: Int) {
        transaction {
            Dish.new {
                this.name = name
                this.stock = stock
                this.cookingTime = cookingTime
                this.price = price
            }
        }
    }

    fun removeDish(id: Int): Boolean {
        return transaction {
            val dish = Dish.find {
                Dishes.id eq id
            }.firstOrNull()

            if (dish == null) {
                return@transaction false
            }

            dish.delete()
            return@transaction true
        }
    }

    fun getAllIncome(): Double {
        val orders = transaction {
            Order.find { Orders.status eq OrderStatus.COMPLETED }.with(Order::dishes).toList()
        }
        return orders.sumOf { order -> order.dishes.sumOf { it.price } }
    }

    fun getMenu(onlyInStock: Boolean = false): List<Dish> {
        val menu = transaction {
            if (onlyInStock) {
                Dish.find { Dishes.stock greater 0 }
            } else {
                Dish.all()
            }.toList()
        }
        return menu
    }

    fun createOrder(user: User, dishes: List<Dish>): Order {
        return transaction {
            dishes.forEach {
                it.stock -= 1
            }

            Order.new {
                this.user = user
                status = OrderStatus.COOKING
                this.dishes = SizedCollection(dishes)
                this.cookingTime = dishes.maxOf { it.cookingTime }
            }
        }
    }

    fun loadOrders(user: User) {
        transaction {
            user.load(User::orders, Order::dishes)
        }
    }

    fun updateOrderCooking(order: Order) {
        transaction {
            order.cookingTime -= 1
            order.refresh(true)
        }
    }

    fun updateOrderStatus(id: Int, status: OrderStatus) {
        transaction {
            Order[id].status = status
        }
    }

    fun getReviews(): List<Review> {
        return transaction {
            Review.all().with(Review::user, Review::order).toList()
        }
    }

    fun createReview(user: User, orderId: Int, mark: Int, comment: String): Review {
        return transaction {
            Review.new {
                this.user = user
                this.order = Order[orderId]
                this.mark = mark
                this.comment = comment
            }
        }
    }
}