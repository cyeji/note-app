package com.example.notes

/**
 * 테스트용 Mock LocalStore 구현
 * 실제 파일 저장 없이 메모리에서만 동작
 */
class MockLocalStore : LocalStore {
    private var notes: List<Note> = emptyList()

    override suspend fun loadAll(): List<Note> = notes

    override suspend fun saveAll(notes: List<Note>) {
        this.notes = notes
    }

    fun clear() {
        notes = emptyList()
    }
}

