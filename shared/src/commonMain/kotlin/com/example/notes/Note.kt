package com.example.notes

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String = "",
    val content: String = "",
    val updatedAt: Long,
    val deleted: Boolean = false
)

// Platform-specific utilities
expect fun generateNoteId(): String
expect fun currentTimeMillis(): Long

// Extension helpers
fun Note.touch(now: Long = currentTimeMillis()) = copy(updatedAt = now)

// Factory function for creating new notes
fun createNote(
    id: String = generateNoteId(),
    title: String = "",
    content: String = "",
    updatedAt: Long = currentTimeMillis(),
    deleted: Boolean = false
) = Note(id, title, content, updatedAt, deleted)

