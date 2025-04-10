package com.oussama.masaratalnur.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // Import Query for ordering
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects // Import toObjects for list deserialization
import com.google.firebase.ktx.Firebase
import com.oussama.masaratalnur.data.model.ContentResult
import com.oussama.masaratalnur.data.model.Topic
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ContentRepositoryImpl : ContentRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    override fun getAllTopics(): Flow<ContentResult<List<Topic>>> = callbackFlow {
        // Emit Loading state immediately
        trySend(ContentResult.Loading).isSuccess

        val topicsCollectionRef = db.collection("topics")
            .orderBy("order", Query.Direction.ASCENDING) // Order by the 'order' field

        val listenerRegistration = topicsCollectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ContentRepositoryImpl", "Listen failed for topics.", error)
                trySend(ContentResult.Error(error)).isSuccess // Emit error
                // Optionally close(error) if it's fatal
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Use toObjects() to deserialize the whole collection into a list
                val topics = snapshot.toObjects<Topic>()
                Log.d("ContentRepositoryImpl", "Topics fetched: ${topics.size} items")
                trySend(ContentResult.Success(topics)).isSuccess // Emit success with the list
            } else {
                // Snapshot is null, which is unusual for collection listeners unless error occurred
                Log.w("ContentRepositoryImpl", "Topics snapshot was null, emitting empty list.")
                // Handle this case - maybe emit empty list or error? Let's emit Success with empty list.
                trySend(ContentResult.Success(emptyList())).isSuccess
            }
        }

        // Remove listener when Flow is cancelled
        awaitClose {
            Log.d("ContentRepositoryImpl", "Closing topics listener.")
            listenerRegistration.remove()
        }
    }

    // Implement other methods (getLessonsForTopic etc.) later
}