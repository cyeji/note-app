package com.example.notes

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

fun Note.touch(now: Long = System.currentTimeMillis()) = copy(updatedAt = now)

