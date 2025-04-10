package com.oussama.masaratalnur.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration // Import if not already
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
// Import toObjects for Category as well
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.oussama.masaratalnur.data.model.Category // Import Category
import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.data.model.Topic
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ContentRepositoryImpl : ContentRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    // Renamed function implementation
    override fun getAllCategories(): Flow<ContentResult<List<Category>>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess

        val categoriesCollectionRef = db.collection("categories") // Fetch from "categories"
            .orderBy("order", Query.Direction.ASCENDING)

        val listenerRegistration = categoriesCollectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ContentRepositoryImpl", "Listen failed for categories.", error)
                trySend(ContentResult.Error(error)).isSuccess
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Deserialize into List<Category>
                val categories = snapshot.toObjects<Category>()
                Log.d("ContentRepositoryImpl", "Categories fetched: ${categories.size} items")
                trySend(ContentResult.Success(categories)).isSuccess
            } else {
                Log.w("ContentRepositoryImpl", "Categories snapshot was null.")
                trySend(ContentResult.Success(emptyList())).isSuccess // Emit empty list
            }
        }
        awaitClose {
            Log.d("ContentRepositoryImpl", "Closing categories listener.")
            listenerRegistration.remove()
        }
    }

    // New function implementation
    override fun getTopicsForCategory(categoryId: String): Flow<ContentResult<List<Topic>>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess

        if (categoryId.isBlank()) {
            Log.w("ContentRepositoryImpl", "categoryId is blank, cannot fetch topics.")
            // Emit error or empty list? Error seems appropriate.
            trySend(ContentResult.Error(IllegalArgumentException("Category ID cannot be blank"))).isSuccess
            close() // Close the flow
            return@callbackFlow
        }

        val topicsCollectionRef = db.collection("topics")
            .whereEqualTo("categoryId", categoryId) // Filter by categoryId
            .orderBy("order", Query.Direction.ASCENDING) // Order within category

        val listenerRegistration = topicsCollectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ContentRepositoryImpl", "Listen failed for topics in category $categoryId.", error)
                trySend(ContentResult.Error(error)).isSuccess
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val topics = snapshot.toObjects<Topic>()
                Log.d("ContentRepositoryImpl", "Topics for category $categoryId fetched: ${topics.size} items")
                trySend(ContentResult.Success(topics)).isSuccess
            } else {
                Log.w("ContentRepositoryImpl", "Topics snapshot for category $categoryId was null.")
                trySend(ContentResult.Success(emptyList())).isSuccess
            }
        }

        awaitClose {
            Log.d("ContentRepositoryImpl", "Closing topics listener for category $categoryId.")
            listenerRegistration.remove()
        }
    }

    // ... (Other future methods) ...
}