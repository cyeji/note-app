package com.example.notes

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class FileLocalStore(private val path: Path = defaultPath()) : LocalStore {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    override suspend fun loadAll(): List<Note> {
        return try {
            if (!Files.exists(path)) return emptyList()
            val text = Files.readString(path)
            if (text.isBlank()) emptyList() else json.decodeFromString<List<Note>>(text)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun saveAll(notes: List<Note>) {
        try {
            val dir = path.parent
            if (dir != null && !Files.exists(dir)) Files.createDirectories(dir)
            val content = json.encodeToString<List<Note>>(notes)
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private fun defaultPath(): Path {
            val home = System.getProperty("user.home")
            return Paths.get(home, ".compose_notes", "notes.json")
        }
    }
}

