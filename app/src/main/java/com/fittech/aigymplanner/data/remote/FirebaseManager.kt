package com.fittech.aigymplanner.data.remote

import com.fittech.aigymplanner.model.UserStats
import com.fittech.aigymplanner.model.WorkoutLog
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import android.net.Uri

class FirebaseManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getCurrentUser() = auth.currentUser

    suspend fun saveUserProfile(uid: String, email: String, username: String, profilePicUrl: String? = null) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to username,
            "profilePicUrl" to profilePicUrl
        )
        db.collection("users").document(uid).set(userMap).await()
    }

    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String {
        val ref = storage.reference.child("profile_pics/$uid.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        return db.collection("users").document(uid).get().await().data
    }

    suspend fun syncLogsToCloud(logs: List<WorkoutLog>) {
        val user = auth.currentUser ?: return
        val batch = db.batch()
        val userLogsRef = db.collection("users").document(user.uid).collection("workout_logs")

        logs.forEach { log ->
            val docRef = userLogsRef.document(log.id)
            batch.set(docRef, log)
        }
        batch.commit().await()
    }

    suspend fun fetchLogsFromCloud(): List<WorkoutLog> {
        val user = auth.currentUser ?: return emptyList()
        return db.collection("users").document(user.uid).collection("workout_logs")
            .get()
            .await()
            .toObjects(WorkoutLog::class.java)
    }

    suspend fun deleteLogFromCloud(logId: String) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("workout_logs").document(logId)
            .delete()
            .await()
    }

    suspend fun saveUserStats(stats: UserStats) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("stats").document("current")
            .set(stats)
            .await()
    }

    suspend fun fetchUserStats(): UserStats? {
        val user = auth.currentUser ?: return null
        return try {
            db.collection("users").document(user.uid).collection("stats").document("current")
                .get()
                .await()
                .toObject(UserStats::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun observeUserStats(onStatsUpdate: (UserStats) -> Unit) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("stats").document("current")
            .addSnapshotListener { snapshot, _ ->
                val stats = snapshot?.toObject(UserStats::class.java)
                if (stats != null) {
                    onStatsUpdate(stats)
                }
            }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithCredential(credential: AuthCredential): Result<Unit> {
        return try {
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
