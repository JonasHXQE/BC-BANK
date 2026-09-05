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
    private val _expenseCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val expenseCategories: StateFlow<List<CategoryItem>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val incomeCategories: StateFlow<List<CategoryItem>> = _incomeCategories.asStateFlow()

    private val _goalCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val goalCategories: StateFlow<List<CategoryItem>> = _goalCategories.asStateFlow()

    private val _budgetCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val budgetCategories: StateFlow<List<CategoryItem>> = _budgetCategories.asStateFlow()

    fun setCategoriesFromCloud(list: List<CategoryItem>, type: CategoryType) {
        when (type) {
            CategoryType.EXPENSE -> _expenseCategories.value = list
            CategoryType.INCOME -> _incomeCategories.value = list
            CategoryType.GOAL -> _goalCategories.value = list
            CategoryType.BUDGET -> _budgetCategories.value = list
        }
    }

    fun clearAllCategories() {
        _expenseCategories.value = emptyList()
        _incomeCategories.value = emptyList()
        _goalCategories.value = emptyList()
        _budgetCategories.value = emptyList()
    }

    fun addCategory(
        name: String,
        type: CategoryType,
        id: String = UUID.randomUUID().toString(),
        icon: String = "DEFAULT"
    ): CategoryItem? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return null
        val newItem = CategoryItem(id = id, name = trimmed, isCustom = true, icon = icon)
        when (type) {
            CategoryType.EXPENSE -> {
                if (_expenseCategories.value.none { it.id == id || it.name.equals(trimmed, ignoreCase = true) }) {
                    _expenseCategories.value = _expenseCategories.value + newItem
                }
            }
            CategoryType.INCOME -> {
                if (_incomeCategories.value.none { it.id == id || it.name.equals(trimmed, ignoreCase = true) }) {
                    _incomeCategories.value = _incomeCategories.value + newItem
                }
            }
            CategoryType.GOAL -> {
                if (_goalCategories.value.none { it.id == id || it.name.equals(trimmed, ignoreCase = true) }) {
                    _goalCategories.value = _goalCategories.value + newItem
                }
            }
            CategoryType.BUDGET -> {
                if (_budgetCategories.value.none { it.id == id || it.name.equals(trimmed, ignoreCase = true) }) {
                    _budgetCategories.value = _budgetCategories.value + newItem
                }
            }
        }
        return newItem
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
