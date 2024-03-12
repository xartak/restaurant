package org.example

class ConsoleHelper {
    fun printList(header: String, choices: Map<Int, String>) {
        println()
        println("$header:")
        choices.forEach { (k, v) ->
            println("$k. $v")
        }
    }

    fun promptMenu(header: String, validChoices: Map<Int, String>, prompt: String = "Введите действие:"): Int {
        while (true) {
            printList(header, validChoices)
            val choice = readInt(prompt, validChoices.map { it.key })
            return choice
        }
    }

    fun <T> promptMenuWithNavigation(header: String, validChoices: Map<Int, Pair<String, () -> T>>): T {
        val action = promptMenu(header, validChoices.mapValues { (_, v) -> v.first })
        return validChoices[action]!!.second()
    }

    fun readDouble(prompt: String? = null): Double {
        while (true) {
            if (prompt != null) {
                print("$prompt ")
            }

            val choice = readln().toDoubleOrNull()
            if (choice == null) {
                println("Пожалуйста, введите число")
                continue
            }

            return choice
        }
    }

    fun readInt(prompt: String? = null, constraints: List<Int> = listOf()): Int {
        while (true) {
            if (prompt != null) {
                print("$prompt ")
            }

            val choice = readln().toIntOrNull()
            if (choice == null) {
                println("Пожалуйста, введите число")
                continue
            }

            if (constraints.isNotEmpty() && !constraints.contains(choice)) {
                println("Пожалуйста, выберите из ${constraints.joinToString(", ")}.")
                continue
            }

            return choice
        }
    }

    fun readString(prompt: String? = null): String {
        while (true) {
            if (prompt != null) {
                print("$prompt ")
            }

            val input = readlnOrNull()
            if (input == null) {
                println("Пожалуйста, введите данные.")
                continue
            }
            return input
        }
    }
}