package com.example.ui.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class CategoryType {
    EXPENSE,
    INCOME,
    GOAL,
    BUDGET
}

data class CategoryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isCustom: Boolean = false,
    val icon: String = "DEFAULT"
)

object CustomCategoryManager {
    private val defaultExpenses = listOf(
        CategoryItem("exp_1", "Alimentación"),
        CategoryItem("exp_2", "Transporte"),
        CategoryItem("exp_3", "Servicios"),
        CategoryItem("exp_4", "Compras"),
        CategoryItem("exp_5", "Entretenimiento"),
        CategoryItem("exp_6", "Salud"),
        CategoryItem("exp_7", "Educación"),
        CategoryItem("exp_8", "Transferencia"),
        CategoryItem("exp_9", "Otro")
    )

    private val defaultIncomes = listOf(
        CategoryItem("inc_1", "Sueldo"),
        CategoryItem("inc_2", "Ventas"),
        CategoryItem("inc_3", "Inversiones"),
        CategoryItem("inc_4", "Depósito"),
        CategoryItem("inc_5", "Transferencia Recibida"),
        CategoryItem("inc_6", "Otro")
    )

    private val defaultGoals = listOf(
        CategoryItem("goal_1", "Fondo de Emergencia", icon = "EMERGENCY"),
        CategoryItem("goal_2", "Viajes y Vacaciones", icon = "TRAVEL"),
        CategoryItem("goal_3", "Tecnología y Gadgets", icon = "TECH"),
        CategoryItem("goal_4", "Hogar y Muebles", icon = "HOME"),
        CategoryItem("goal_5", "Vehículo / Auto", icon = "CAR"),
        CategoryItem("goal_6", "Ahorro General", icon = "SAVINGS")
    )

    private val defaultBudgets = listOf(
        CategoryItem("bud_1", "Alimentación"),
        CategoryItem("bud_2", "Transporte"),
        CategoryItem("bud_3", "Servicios"),
        CategoryItem("bud_4", "Entretenimiento"),
        CategoryItem("bud_5", "Salud"),
        CategoryItem("bud_6", "Educación")
    )

    private val _expenseCategories = MutableStateFlow(defaultExpenses)
    val expenseCategories: StateFlow<List<CategoryItem>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow(defaultIncomes)
    val incomeCategories: StateFlow<List<CategoryItem>> = _incomeCategories.asStateFlow()

    private val _goalCategories = MutableStateFlow(defaultGoals)
    val goalCategories: StateFlow<List<CategoryItem>> = _goalCategories.asStateFlow()

    private val _budgetCategories = MutableStateFlow(defaultBudgets)
    val budgetCategories: StateFlow<List<CategoryItem>> = _budgetCategories.asStateFlow()

    fun addCategory(name: String, type: CategoryType) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val newItem = CategoryItem(name = trimmed, isCustom = true)
        when (type) {
            CategoryType.EXPENSE -> _expenseCategories.value = _expenseCategories.value + newItem
            CategoryType.INCOME -> _incomeCategories.value = _incomeCategories.value + newItem
            CategoryType.GOAL -> _goalCategories.value = _goalCategories.value + newItem
            CategoryType.BUDGET -> _budgetCategories.value = _budgetCategories.value + newItem
        }
    }

    fun deleteCategory(id: String, type: CategoryType) {
        when (type) {
            CategoryType.EXPENSE -> _expenseCategories.value = _expenseCategories.value.filter { it.id != id }
            CategoryType.INCOME -> _incomeCategories.value = _incomeCategories.value.filter { it.id != id }
            CategoryType.GOAL -> _goalCategories.value = _goalCategories.value.filter { it.id != id }
            CategoryType.BUDGET -> _budgetCategories.value = _budgetCategories.value.filter { it.id != id }
        }
    }
}
