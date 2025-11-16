package com.example.notes

interface LocalStore {
    suspend fun loadAll(): List<Note>
    suspend fun saveAll(notes: List<Note>)
}
