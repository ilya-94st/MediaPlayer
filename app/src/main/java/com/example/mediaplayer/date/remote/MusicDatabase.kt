package com.example.mediaplayer.date.remote

import com.example.mediaplayer.date.entinities.Song
import com.example.mediaplayer.other.Constants.SONGS_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val fireStore = FirebaseFirestore.getInstance()

    private val collection = Firebase.firestore.collection(SONGS_COLLECTION)

    suspend fun getAllSongs() : List<Song> {
        return try {
            collection.get().await().toObjects(Song::class.java)
        } catch (e: Exception){
            emptyList()
        }
    }
}