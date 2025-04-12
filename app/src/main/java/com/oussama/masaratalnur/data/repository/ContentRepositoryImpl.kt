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
import com.oussama.masaratalnur.data.model.ContentStatus
import com.oussama.masaratalnur.data.model.Lesson
import com.oussama.masaratalnur.data.model.Topic
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContentRepositoryImpl : ContentRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    private val categoriesCollection = db.collection("categories")
    private val topicsCollection = db.collection("topics")
    private val lessonsCollection = db.collection("lessons")

    // --- Categories Impl ---
    override fun getAllCategories(): Flow<ContentResult<List<Category>>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess

        categoriesCollection.orderBy("order", Query.Direction.ASCENDING)

        val listenerRegistration = categoriesCollection.addSnapshotListener { snapshot, error ->
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

    override fun getCategory(categoryId: String): Flow<ContentResult<Category>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess

        if (categoryId.isBlank()) {
            trySend(ContentResult.Error(IllegalArgumentException("Category ID cannot be blank"))).isSuccess
            close()
            return@callbackFlow
        }

        val docRef = categoriesCollection.document(categoryId)

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

    override suspend fun addCategory(category: Category): Result<Unit> {
        return try {
            // Create new doc with auto-ID, set data EXCEPT the ID field itself
            categoriesCollection.add(category.copy(id = "")).await() // Use add() for auto-ID
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
            categoriesCollection.document(category.id).set(category).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error updating category ${category.id}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            if (categoryId.isBlank()) return Result.failure(IllegalArgumentException("Category ID required for delete"))
            categoriesCollection.document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error deleting category $categoryId", e)
            Result.failure(e)
        }
    }


    // --- Topics Impl ---
    override fun getTopicsForCategory(categoryId: String): Flow<ContentResult<List<Topic>>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess

        if (categoryId.isBlank()) {
            Log.w("ContentRepositoryImpl", "categoryId is blank, cannot fetch topics.")
            // Emit error or empty list? Error seems appropriate.
            trySend(ContentResult.Error(IllegalArgumentException("Category ID cannot be blank"))).isSuccess
            close() // Close the flow
            return@callbackFlow
        }

        var query = topicsCollection.whereEqualTo("categoryId", categoryId) // Filter by categoryId
            .orderBy("order", Query.Direction.ASCENDING) // Order within category

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
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


    // --- Lessons Impl ---
    override fun getLessonsForTopic(topicId: String): Flow<ContentResult<List<Lesson>>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess
        if (topicId.isBlank()) {
            trySend(ContentResult.Error(IllegalArgumentException("Topic ID cannot be blank"))).isSuccess
            close(); return@callbackFlow
        }
        // Query for PUBLISHED lessons only for regular users
        // TODO: Modify later based on user role if drafts need to be shown to editors
        val query = lessonsCollection
            .whereEqualTo("topicId", topicId)
            .whereEqualTo("status", ContentStatus.PUBLISHED.name) // Query by enum name string
            .orderBy("order", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ContentRepositoryImpl", "Listen failed for Lesson in topicId $topicId.", error)
                trySend(ContentResult.Error(error)).isSuccess
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val lessons = snapshot.toObjects<Lesson>()
                Log.d("ContentRepositoryImpl", "Lessons for topic $topicId fetched: ${lessons.size} items")
                trySend(ContentResult.Success(lessons)).isSuccess
            } else {
                Log.w("ContentRepositoryImpl", "Lessons snapshot for topic $topicId was null.")
                trySend(ContentResult.Success(emptyList())).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getAllLessons(): Flow<ContentResult<List<Lesson>>> = callbackFlow {
        // WARNING: Fetching ALL lessons might be inefficient. Use with caution or pagination.
        // Typically for Admin use. Does not filter by status here, assumes admin context.
        trySend(ContentResult.Loading).isSuccess
        val query = lessonsCollection.orderBy("topicId").orderBy("order", Query.Direction.ASCENDING)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(ContentResult.Error(error)).isSuccess; return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(ContentResult.Success(snapshot.toObjects<Lesson>())).isSuccess
            } else {
                trySend(ContentResult.Success(emptyList())).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getLesson(lessonId: String): Flow<ContentResult<Lesson>> = callbackFlow {
        trySend(ContentResult.Loading).isSuccess
        if (lessonId.isBlank()) {
            trySend(ContentResult.Error(IllegalArgumentException("Lesson ID cannot be blank"))).isSuccess
            close(); return@callbackFlow
        }
        val docRef = lessonsCollection.document(lessonId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            // ... handle snapshot/error, similar to getTopic/getCategory ...
            if (error != null) {
                trySend(ContentResult.Error(error)).isSuccess; return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val lesson = try { snapshot.toObject<Lesson>() } catch (e: Exception) { null }
                if (lesson != null) trySend(ContentResult.Success(lesson)).isSuccess
                else trySend(ContentResult.Error(Exception("Failed to parse lesson data"))).isSuccess
            } else {
                trySend(ContentResult.Error(NoSuchElementException("Lesson not found"))).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addLesson(lesson: Lesson): Result<Unit> {
        return try {
            if (lesson.topicId.isBlank()) return Result.failure(IllegalArgumentException("Topic ID required for lesson"))
            // Add will serialize the lesson, including its status enum as a string
            lessonsCollection.add(lesson.copy(id = "")).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error adding lesson", e)
            Result.failure(e)
        }
    }

    override suspend fun updateLesson(lesson: Lesson): Result<Unit> {
        return try {
            if (lesson.id.isBlank()) return Result.failure(IllegalArgumentException("Lesson ID required for update"))
            if (lesson.topicId.isBlank()) return Result.failure(IllegalArgumentException("Topic ID required for lesson"))
            // Set will serialize the lesson, including its status enum as a string
            lessonsCollection.document(lesson.id).set(lesson).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error updating lesson ${lesson.id}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteLesson(lessonId: String): Result<Unit> {
        return try {
            if (lessonId.isBlank()) return Result.failure(IllegalArgumentException("Lesson ID required for delete"))
            lessonsCollection.document(lessonId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContentRepositoryImpl", "Error deleting lesson $lessonId", e)
            Result.failure(e)
        }
    }

    // ... Quizzes Impl ...
}