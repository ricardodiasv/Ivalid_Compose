package com.example.ivalid_compose.ui.orders

import androidx.compose.material3.DatePicker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivalid_compose.R
import com.example.ivalid_compose.ui.home.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OrderItem(
    val name: String,
    val quantity: Int,
    val subtotal: Double
)

data class Order(
    val id: String,
    val date: String,
    val total: Double,
    val status: String,
    val items: List<OrderItem>
)

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class OrdersViewModel: ViewModel(){
    var uiState by mutableStateOf(OrdersUiState(orders = mockOrders))
        private set

    init{
        fetchOrders()
    }

    private fun fetchOrders(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null){
            uiState = uiState.copy(
                isLoading = false,
                error = "Usuário não autenticado. Faça login novamente!"
            )
            return
        }

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try{
                val db = FirebaseFirestore.getInstance()

                val result = db.collection("pedidos")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val ordersList = result.documents.mapNotNull { document ->
                    val total = (document.get("total") as? Number)?.toDouble() ?: 0.0
                    val status = document.getString("status") ?: "Status desconhecido"
                    val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                    val dateStr = dateFormat.format(timestamp)

                    @Suppress("UNCHECKED_CAST")
                    val itemsMaps = document.get("itens") as? List<Map<String, Any>> ?: emptyList()
                    val ordersItems = itemsMaps.map { itemMap ->
                        OrderItem(
                            name = itemMap["name"] as? String ?: "Item Desconhecido",
                            quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0,
                            subtotal = (itemMap["subtotal"] as? Number)?.toDouble() ?: 0.0
                        )
                    }

                    Order(
                        id = document.id,
                        date = dateStr,
                        total = total,
                        status = status,
                        items = ordersItems
                    )
                }

                uiState = uiState.copy(
                    orders = ordersList,
                    isLoading = false
                )
            } catch (e: Exception){
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Erro ao carregar pedidos: ${e.localizedMessage}"
                )
            }
        }
    }
}

private val mockProducts = listOf(
    Product(
        id = "p1",
        name = "Pão Francês 500g",
        priceNow = 4.99,
        priceOriginal = 8.49,
        expiresInDays = 1,
        isFavorite = false,
        imageRes = R.drawable.pao_frances,
        distanceKm = 0.8,
        storeName = "Padaria Central",
        brand = "Visconti",
        categoryId = "Padaria"
    ),
    Product(
        id = "p2",
        name = "Milho Cozido 200g",
        priceNow = 2.25,
        priceOriginal = 5.30,
        expiresInDays = 3,
        isFavorite = false,
        imageRes = R.drawable.milho_lata,
        distanceKm = 1.2,
        storeName = "Mercado Goiás",
        brand = "Quero",
        categoryId = "Enlatados"
    ),
    Product(
        id = "p3",
        name = "Vinho Tinto 750ml",
        priceNow = 29.90,
        priceOriginal = 35.50,
        expiresInDays = 5,
        isFavorite = true,
        imageRes = R.drawable.vinho,
        distanceKm = 3.1,
        storeName = "Assaí",
        brand = "Pérgola",
        categoryId = "Bebidas"
    )
)

private val mockOrders = listOf(
    Order(
        id = "ABC12345",
        date = "25/10/2025",
        total = 75.50,
        status = "Pagamento Pendente",
        items = listOf(
            OrderItem(mockProducts[0].name, 5, 24.95),
            OrderItem(mockProducts[1].name, 2, 4.50),
            OrderItem(mockProducts[2].name, 1, 46.05)
        )
    ),
)