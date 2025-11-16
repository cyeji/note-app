package com.example.notes

class NoteRepository(private val store: LocalStore) {
    private var cache: MutableList<Note> = mutableListOf()
    private var initialized = false

    suspend fun init() {
        if (!initialized) {
            cache = store.loadAll().toMutableList()
            initialized = true
        }
    }

    suspend fun getAll(): List<Note> {
        init()
        return cache.filter { !it.deleted }.sortedByDescending { it.updatedAt }
    }

    suspend fun get(id: String): Note? {
        init()
        return cache.find { it.id == id }
    }

    suspend fun upsert(note: Note) {
        init()
        val idx = cache.indexOfFirst { it.id == note.id }
        if (idx >= 0) cache[idx] = note
        else cache.add(note)
        persist()
    }

    suspend fun delete(id: String) {
        init()
        val idx = cache.indexOfFirst { it.id == id }
        if (idx >= 0) {
            cache[idx] = cache[idx].copy(deleted = true, updatedAt = currentTimeMillis())
            persist()
        }
    }

    private suspend fun persist() {
        store.saveAll(cache)
    }
}
