package com.example.notes

import java.util.UUID

actual fun generateNoteId(): String = UUID.randomUUID().toString()

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

