package com.example.ivalid_compose.repository

import androidx.compose.animation.core.snap
import com.example.ivalid_compose.ui.home.Category
import com.example.ivalid_compose.ui.home.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository{
    private val db = FirebaseFirestore.getInstance()

    suspend fun getProducts(): List<Product>{
        return try{
            val snapshot = db.collection("produtos").get().await()
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception){
            emptyList()
        }
    }

    suspend fun getCategories(): List<Category> {
        return try {
            val snapshot = db.collection("categories").get().await()
            snapshot.toObjects(Category::class.java)
        } catch (e: Exception){
            emptyList()
        }
    }
}