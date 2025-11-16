package com.example.notes

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeServer {
    private val data = mutableMapOf<String, Note>()
    private val mutex = Mutex()

    suspend fun loadAll(): List<Note> = mutex.withLock {
        data.values.toList()
    }

    suspend fun upsert(note: Note) = mutex.withLock {
        val existing = data[note.id]
        if (existing == null || note.updatedAt >= existing.updatedAt) {
            data[note.id] = note
        }
    }

    suspend fun replaceAll(notes: List<Note>) = mutex.withLock {
        data.clear()
        notes.forEach { data[it.id] = it }
    }

    suspend fun clear() = mutex.withLock {
        data.clear()
    }
}


