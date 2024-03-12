package db.enums

enum class OrderStatus(val title: String) {
    COOKING("Готовится"),
    AWAITING_PAYMENT("Готов, ожидает оплаты"),
    COMPLETED("Завершен"),
    CANCELLED("Отменен")
}