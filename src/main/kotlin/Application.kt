package org.example

import db.Database
import db.dao.User
import db.enums.OrderStatus
import db.enums.UserType
import org.example.db.dao.Dish
import org.example.db.dao.Order
import utils.md5
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class Application {
    private val consoleHelper = ConsoleHelper()
    private val database = Database()

    private var currentUser: User? = null

    fun start() {
        while (true) {
            consoleHelper.promptMenuWithNavigation(
                "Вход в меню ресторана", mapOf(
                    1 to ("Войти" to ::login),
                    2 to ("Зарегистрироваться" to ::register),
                    0 to ("Завершение работы" to ::exit)
                )
            )
        }
    }

    private fun loginUser(user: User) {
        currentUser = user
        println("Добро пожаловать, ${user.type.title} ${user.username}!")
        when (user.type) {
            UserType.VISITOR -> showVisitorMenu()
            UserType.ADMIN -> showAdminMenu()
        }
    }

    private fun login() {
        val username = consoleHelper.readString("Введите имя пользователя:")
        val password = consoleHelper.readString("Введите пароль:")

        val user = database.getUser(username, password)

        if (user == null) {
            println("Неверные имя пользователя или пароль.")
        } else {
            loginUser(user)
        }
    }

    private fun register() {
        val username = consoleHelper.readString("Введите имя пользователя:")
        
        if (database.usernameExists(username)) {
            println("Пользователь с таким именем уже существует.")
        } else {
            val password = consoleHelper.readString("Введите пароль:")
            val choice = consoleHelper.promptMenu("Введите тип пользователя", mapOf(
                1 to "Посетитель",
                2 to "Администратор"
            ))

            val userType = when (choice) {
                1 -> UserType.VISITOR
                2 -> UserType.ADMIN
                else -> throw Exception() // недостижимо
            }

            val user = database.addUser(username, password.md5(), userType)
            loginUser(user)
        }
    }

    private fun showAdminMenu() {
        while (currentUser != null) {
            consoleHelper.promptMenuWithNavigation("Меню администратора", mapOf(
                1 to ("Текущее меню" to { consoleHelper.printList("Текущее меню", getFormattedMenu()) }),
                2 to ("Добавить блюдо" to ::addDish),
                3 to ("Удалить блюдо" to ::removeDish),
                4 to ("Выручка за весь период" to ::showIncome),
                5 to ("Отзывы клиентов" to ::showReviews),
                0 to ("Выход из аккаунта" to {
                    currentUser = null
                }),
            ))
        }
    }

    private fun addDish() {
        val name = consoleHelper.readString("Введите название блюда:")
        val price = consoleHelper.readDouble("Введите стоимость:")
        val stock = consoleHelper.readInt("Введите количество в наличии:")
        val cookingTime = consoleHelper.readInt("Введите время приготовления в минутах:")

        database.addDish(name, price, stock, cookingTime)
        println("Блюдо успешно добавлено.")
    }

    private fun removeDish() {
        val dishes = database.getMenu()

        val id = consoleHelper.promptMenu(
            "Текущее меню",
            dishes.associate {
                it.id.value to it.format()
            },
            "Введите номер блюда для удаления:"
        )
        database.removeDish(id)
        println("Блюдо успешно удалено.")
    }

    private fun showIncome() {
        val income = database.getAllIncome()
        println("Выручка ресторана: $income")
    }

    private fun showReviews() {
        val reviews = database.getReviews()
        if (reviews.isEmpty()) {
            println("Нет отзывов.")
        } else {
            reviews.forEach {
                println("${it.id}. Оценка от пользователя ${it.user.username} на заказ ${it.order.id.value}: " +
                        "${it.mark}. Комментарий: ${it.comment}")
            }
        }
    }

    private fun showVisitorMenu() {
        while (currentUser != null) {
            consoleHelper.promptMenuWithNavigation("Меню посетителя", mapOf(
                1 to ("Текущее меню" to { consoleHelper.printList("Текущее меню", getFormattedMenu()) }),
                2 to ("Сделать заказ" to ::createOrder),
                3 to ("Список заказов" to { showOrders() }),
                4 to ("Оплатить заказ" to ::payOrder),
                5 to ("Отменить заказ" to ::cancelOrder),
                6 to ("Оставить отзыв" to ::reviewOrder),
                0 to ("Выход из аккаунта" to {
                    currentUser = null
                }),
            ))
        }
    }

    private fun createOrder() {
        val cart = mutableListOf<Dish>()

        while (true) {
            if (cart.isNotEmpty()) {
                println()
                println("Текущий состав заказа:")
                cart.forEachIndexed { index, dish ->
                    println("${index + 1}. ${dish.name} - ${dish.price} руб.")
                }
                println("Итого: ${cart.sumOf { it.price }}")
            }

            val action = consoleHelper.promptMenu("Создание заказа", mapOf(
                1 to "Добавить блюдо",
                2 to "Удалить блюдо",
                3 to "Создать заказ",
                0 to "Отмена"
            ))

            when (action) {
                1 -> {
                    val dishes = database.getMenu(true)
                    val dishId = consoleHelper.promptMenu(
                        "Добавление блюда в заказ",
                        dishes.associate {
                            it.id.value to it.format()
                        },
                        "Введите номер блюда:"
                    )

                    if (cart.find { it.id.value == dishId } != null) {
                        println("Блюдо уже добавлено в корзину.")
                    } else {
                        val dish = dishes.first { it.id.value == dishId }
                        cart.add(dish)
                        println("Блюдо добавлено.")
                    }
                }
                2 -> {
                    if (cart.isEmpty()) {
                        println("В корзине ничего нет.")
                    } else {
                        val index = consoleHelper.readInt("Введите номер позиции для удаления:")
                        if (index in 1..cart.size) {
                            cart.removeAt(index)
                        } else {
                            println("Введён неверный номер.")
                        }
                    }
                }
                3 -> {
                    if (cart.isEmpty()) {
                        println("В корзине ничего нет.")
                    } else {
                        break
                    }
                }
                0 -> return
            }
        }

        val order = database.createOrder(currentUser!!, cart)
        thread {
            cookOrder(order)
        }
    }

    private fun cookOrder(order: Order) {
        while (order.cookingTime > 0 && order.status == OrderStatus.COOKING) {
            Thread.sleep(3000)
            database.updateOrderCooking(order)
        }

        if (order.cookingTime == 0 && order.status == OrderStatus.COOKING) {
            database.updateOrderStatus(order.id.value, OrderStatus.AWAITING_PAYMENT)
        }
    }

    private fun showOrders(): List<Order>? {
        database.loadOrders(currentUser!!)
        val orders = currentUser!!.orders

        if (orders.empty()) {
            println("Нет заказов.")
            return null
        } else {
            println("Заказы:")
            orders.forEach { order ->
                println(order.format())
                order.dishes.forEachIndexed { index, dish ->
                    println("\t${order.id.value}.${index + 1} - ${dish.name}: ${dish.price} руб.")
                }
            }
        }
        return orders.toList()
    }

    private fun payOrder() {
        database.loadOrders(currentUser!!)
        val orders = currentUser!!.orders.filter { it.status == OrderStatus.AWAITING_PAYMENT }
        if (orders.isNotEmpty()) {
            val id = consoleHelper.promptMenu(
                "Неоплаченные заказы",
                orders.associate {
                    it.id.value to it.format()
                },
                "Введите номер заказа для оплаты:"
            )
            database.updateOrderStatus(id, OrderStatus.COMPLETED)
            println("Заказ оплачен.")
        } else {
            println("Нет доступных для оплаты заказов.")
        }
    }

    private fun cancelOrder() {
        database.loadOrders(currentUser!!)
        val orders = currentUser!!.orders.filter { it.status == OrderStatus.COOKING }
        if (orders.isNotEmpty()) {
            val id = consoleHelper.promptMenu(
                "Готовящиеся заказы",
                orders.associate {
                    it.id.value to it.format()
                },
                "Введите номер заказа для отмены:"
            )
            database.updateOrderStatus(id, OrderStatus.CANCELLED)
            println("Заказ отменён.")
        } else {
            println("Нет доступных для отмены заказов.")
        }
    }

    private fun reviewOrder() {
        database.loadOrders(currentUser!!)
        val orders = currentUser!!.orders.filter { it.status == OrderStatus.COMPLETED }
        if (orders.isNotEmpty()) {
            val orderId = consoleHelper.promptMenu(
                "Заказы, о которых можно оставить отзыв",
                orders.associate {
                    it.id.value to it.format()
                },
                "Введите номер заказа:"
            )
            val mark = consoleHelper.readInt("Поставьте оценку от 1 до 5:", (1..5).toList())
            val comment = consoleHelper.readString("Оставьте комментарий:")

            database.createReview(currentUser!!, orderId, mark, comment)
            println("Ваш отзыв сохранён.")
        } else {
            println("Нет заказов, доступных для отзыва.")
        }
    }

    private fun getFormattedMenu(): Map<Int, String> {
        val menu = database.getMenu()
        return menu.associate {
            it.id.value to "${it.name} - ${it.price} * ${it.stock}. Время приготовления - ${it.cookingTime} мин"
        }
    }

    private fun exit() {
        println("Хорошего дня!")
        exitProcess(0)
    }
}