package com.oussama.masaratalnur.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
// Import toObjects for Category as well
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.oussama.masaratalnur.data.model.Category // Import Category
import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.data.model.Topic
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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

    override suspend fun addCategory(category: Category): Result<Unit> {
        return try {
            // Create new doc with auto-ID, set data EXCEPT the ID field itself
            db.collection("categories").add(category.copy(id = "")).await() // Use add() for auto-ID
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error adding category", e)
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            if (category.id.isBlank()) return Result.failure(IllegalArgumentException("Category ID required for update"))
            // Use set with merge=true or update? Set is simpler if sending whole object.
            db.collection("categories").document(category.id).set(category).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error updating category ${category.id}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            if (categoryId.isBlank()) return Result.failure(IllegalArgumentException("Category ID required for delete"))
            db.collection("categories").document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error deleting category $categoryId", e)
            Result.failure(e)
        }
    }

    override fun getCategory(categoryId: String): Flow<ContentResult<Category>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess

        if (categoryId.isBlank()) {
            trySend(ContentResult.Error(IllegalArgumentException("Category ID cannot be blank"))).isSuccess
            close()
            return@callbackFlow
        }

        val docRef = db.collection("categories").document(categoryId)

        val listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ContentRepositoryImpl", "Listen failed for category $categoryId.", error)
                trySend(ContentResult.Error(error)).isSuccess
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val category: Category? = try { snapshot.toObject<Category>() } catch (e: Exception) { null }
                if (category != null) {
                    Log.d("ContentRepositoryImpl", "Category $categoryId fetched: $category")
                    trySend(ContentResult.Success(category)).isSuccess
                } else {
                    // Deserialization failed somehow, treat as error or not found
                    Log.e("ContentRepositoryImpl", "Failed to deserialize category $categoryId.")
                    trySend(ContentResult.Error(Exception("Failed to parse category data."))).isSuccess
                }
            } else {
                // Document doesn't exist
                Log.w("ContentRepositoryImpl", "Category $categoryId does not exist.")
                trySend(ContentResult.Error(NoSuchElementException("Category not found."))).isSuccess
            }
        }

        awaitClose {
            Log.d("ContentRepositoryImpl", "Closing category listener for $categoryId.")
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

    override fun getAllTopics(): Flow<ContentResult<List<Topic>>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess
        val query = topicsCollection.orderBy("categoryId").orderBy("order", Query.Direction.ASCENDING) // Example ordering
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(ContentResult.Error(error)).isSuccess; return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(ContentResult.Success(snapshot.toObjects<Topic>())).isSuccess
            } else {
                trySend(ContentResult.Success(emptyList())).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }


    override fun getTopic(topicId: String): Flow<ContentResult<Topic>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess
        if (topicId.isBlank()) {
            trySend(ContentResult.Error(IllegalArgumentException("Topic ID cannot be blank"))).isSuccess
            close(); return@callbackFlow
        }
        val docRef = topicsCollection.document(topicId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(ContentResult.Error(error)).isSuccess; return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val topic = try { snapshot.toObject<Topic>() } catch (e: Exception) { null }
                if (topic != null) trySend(ContentResult.Success(topic)).isSuccess
                else trySend(ContentResult.Error(Exception("Failed to parse topic data"))).isSuccess
            } else {
                trySend(ContentResult.Error(NoSuchElementException("Topic not found"))).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addTopic(topic: Topic): Result<Unit> {
        return try {
            // Ensure categoryId is set before adding
            if (topic.categoryId.isBlank()) return Result.failure(IllegalArgumentException("Category ID required for topic"))
            // Add document with auto-generated ID
            topicsCollection.add(topic.copy(id = "")).await() // Add with auto-ID
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error adding topic", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTopic(topic: Topic): Result<Unit> {
        return try {
            if (topic.id.isBlank()) return Result.failure(IllegalArgumentException("Topic ID required for update"))
            if (topic.categoryId.isBlank()) return Result.failure(IllegalArgumentException("Category ID required for topic"))
            topicsCollection.document(topic.id).set(topic).await() // Use set to overwrite
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error updating topic ${topic.id}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTopic(topicId: String): Result<Unit> {
        return try {
            if (topicId.isBlank()) return Result.failure(IllegalArgumentException("Topic ID required for delete"))
            topicsCollection.document(topicId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error deleting topic $topicId", e)
            Result.failure(e)
        }
    }

    // ... (Other future methods) ...
}