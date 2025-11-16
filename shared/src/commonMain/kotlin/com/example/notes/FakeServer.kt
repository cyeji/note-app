package com.example.notes

class FakeServer {
    private val data = mutableMapOf<String, Note>()

    @Synchronized
    fun loadAll(): List<Note> = data.values.toList()

    @Synchronized
    fun upsert(note: Note) {
        val existing = data[note.id]
        if (existing == null || note.updatedAt >= existing.updatedAt) {
            data[note.id] = note
        }
    }

    @Synchronized
    fun replaceAll(notes: List<Note>) {
        data.clear()
        notes.forEach { data[it.id] = it }
    }

    @Synchronized
    fun clear() = data.clear()
}


