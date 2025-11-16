package com.example.notes

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class AndroidLocalStore(private val context: Context, private val fileName: String = "notes.json") : LocalStore {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private fun file(): File = File(context.filesDir, fileName)

    override suspend fun loadAll(): List<Note> {
        return try {
            val f = file()
            if (!f.exists()) return emptyList()
            val text = f.readText()
            if (text.isBlank()) emptyList() else json.decodeFromString(text)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun saveAll(notes: List<Note>) {
        try {
            val f = file()
            f.writeText(json.encodeToString(notes))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

