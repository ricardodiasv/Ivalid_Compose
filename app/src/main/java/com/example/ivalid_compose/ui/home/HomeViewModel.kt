package com.example.ivalid_compose.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivalid_compose.repository.ProductRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class Category(
    val id: String = "",
    val name: String = "",
    val icon: Int? = null
)
enum class ProductSortOption {
    DEFAULT,
    PRICE_ASC,      // Menor Preço (Ascendente)
    PRICE_DESC,     // Maior Preço (Descendente)
    DISCOUNT_ASC,   // Menor Desconto (Ascendente)
    DISCOUNT_DESC,  // Maior Desconto (Descendente)
    DISTANCE_ASC,   // Mais Próximo (Menor KM)
    DISTANCE_DESC   // Mais Distante (Maior KM)
}


data class Product(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val urlImagem: String = "",
    val storeName: String = "",
    val distanceKm: Double = 0.0,
    val priceOriginal: Double = 0.0,
    val priceNow: Double = 0.0,
    val expiresInDays: Int = 0,
    val categoryId: String = "",
    val isFavorite: Boolean = false
) {
    val discountPercent: Int
        get() = if (priceOriginal > 0) {
            val res = ((priceOriginal - priceNow) / priceOriginal) * 100
            if (res.isNaN()) 0 else res.roundToInt().coerceAtLeast(0)
        } else 0
}

data class HomeUiState(
    val query: String = "",
    val selectedCategoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val currentSort: ProductSortOption = ProductSortOption.DEFAULT
)

class HomeViewModel (private val repository: ProductRepository = ProductRepository()) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        loadFromFirestore()
    }

    private fun loadFromFirestore(){
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            val products = repository.getProducts()
            val categories = repository.getCategories()

            uiState = uiState.copy(
                allProducts = products,
                categories = if(categories.isEmpty()) uiState.categories else categories,
                isLoading = false
            )
            applyFilters()
        }
    }

    fun onQueryChange(new: String) {
        uiState = uiState.copy(query = new)
        applyFilters()
    }

    fun onSelectCategory(id: String?) {
        uiState = uiState.copy(selectedCategoryId = id)
        applyFilters()
    }

    fun toggleFavorite(productId: String) {
        val updated = uiState.allProducts.map {
            if (it.id == productId) it.copy(isFavorite = !it.isFavorite) else it
        }
        uiState = uiState.copy(allProducts = updated)
        applyFilters()
    }

    fun sortProducts(sortOption: ProductSortOption) {
        uiState = uiState.copy(currentSort = sortOption)
        applyFilters()
    }

    private fun applyFilters() {
        val q = uiState.query.trim().lowercase()
        val cat = uiState.selectedCategoryId

        var filtered = uiState.allProducts.filter { p ->
            val matchesQuery = q.isEmpty() ||
                    p.name.lowercase().contains(q) ||
                    p.brand.lowercase().contains(q) ||
                    p.storeName.lowercase().contains(q)
            val matchesCat = (cat == null || cat == "all") || p.categoryId == cat
            matchesQuery && matchesCat
        }

        filtered = when(uiState.currentSort){
            ProductSortOption.PRICE_ASC -> filtered.sortedBy { it.priceNow }
            ProductSortOption.PRICE_DESC -> filtered.sortedByDescending { it.priceNow }

            ProductSortOption.DISCOUNT_ASC -> filtered.sortedBy { it.discountPercent }
            ProductSortOption.DISCOUNT_DESC -> filtered.sortedByDescending { it.discountPercent }

            ProductSortOption.DISTANCE_ASC -> filtered.sortedBy { it.distanceKm }
            ProductSortOption.DISTANCE_DESC -> filtered.sortedByDescending { it.distanceKm }

            ProductSortOption.DEFAULT -> filtered.sortedWith (
                compareBy<Product> {it.expiresInDays}
                    .thenByDescending { it.discountPercent }
            )
        }

        uiState = uiState.copy(filteredProducts = filtered)
    }
}