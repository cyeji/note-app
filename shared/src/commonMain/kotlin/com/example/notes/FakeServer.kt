package com.example.notes
}
    fun clear() = data.clear()
    @Synchronized

    }
        notes.forEach { data[it.id] = it }
        data.clear()
    fun replaceAll(notes: List<Note>) {
    @Synchronized

    }
        }
            data[note.id] = note
        if (existing == null || note.updatedAt >= existing.updatedAt) {
        val existing = data[note.id]
    fun upsert(note: Note) {
    @Synchronized

    fun loadAll(): List<Note> = data.values.toList()
    @Synchronized

    private val data = mutableMapOf<String, Note>()
class FakeServer {


