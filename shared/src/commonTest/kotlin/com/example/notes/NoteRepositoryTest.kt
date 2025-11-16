package com.example.notes

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class NoteRepositoryTest {
    private lateinit var mockStore: MockLocalStore
    private lateinit var repository: NoteRepository

    @BeforeTest
    fun setup() {
        mockStore = MockLocalStore()
        repository = NoteRepository(mockStore)
        // 테스트 카운터 초기화
        testNoteIdCounter = 0
        testTimeMillis = 1000L
    }

    @Test
    fun testGetAll_EmptyList() = runTest {
        repository.init()
        val notes = repository.getAll()
        assertTrue(notes.isEmpty(), "초기 상태에서는 빈 리스트여야 함")
    }

    @Test
    fun testUpsert_AddNewNote() = runTest {
        val note = createTestNote(title = "Test Note", content = "Test Content", updatedAt = testTimeMillis)
        repository.upsert(note)
        
        val notes = repository.getAll()
        assertEquals(1, notes.size, "노트가 1개 추가되어야 함")
        assertEquals("Test Note", notes[0].title, "제목이 일치해야 함")
        assertEquals("Test Content", notes[0].content, "내용이 일치해야 함")
    }

    @Test
    fun testUpsert_UpdateExistingNote() = runTest {
        val note1 = createTestNote(id = "1", title = "Original", updatedAt = 1000L)
        repository.upsert(note1)
        
        val note2 = createTestNote(id = "1", title = "Updated", updatedAt = 2000L)
        repository.upsert(note2)
        
        val notes = repository.getAll()
        assertEquals(1, notes.size, "노트가 1개여야 함 (업데이트)")
        assertEquals("Updated", notes[0].title, "제목이 업데이트되어야 함")
    }

    @Test
    fun testDelete_SoftDelete() = runTest {
        val note = createTestNote(id = "1", title = "To Delete", updatedAt = testTimeMillis)
        repository.upsert(note)
        
        repository.delete("1")
        
        val notes = repository.getAll()
        assertTrue(notes.isEmpty(), "삭제된 노트는 목록에 나타나지 않아야 함")
    }

    @Test
    fun testGetAll_SortedByUpdatedAt() = runTest {
        val note1 = createTestNote(id = "1", title = "First", updatedAt = 1000L)
        val note2 = createTestNote(id = "2", title = "Second", updatedAt = 2000L)
        val note3 = createTestNote(id = "3", title = "Third", updatedAt = 1500L)
        
        repository.upsert(note1)
        repository.upsert(note2)
        repository.upsert(note3)
        
        val notes = repository.getAll()
        assertEquals(3, notes.size, "노트가 3개여야 함")
        assertEquals("Second", notes[0].title, "최신순으로 정렬되어야 함 (updatedAt: 2000)")
        assertEquals("Third", notes[1].title, "두 번째는 updatedAt: 1500")
        assertEquals("First", notes[2].title, "세 번째는 updatedAt: 1000")
    }

    @Test
    fun testGet_ById() = runTest {
        val note1 = createTestNote(id = "1", title = "Note 1", updatedAt = testTimeMillis)
        val note2 = createTestNote(id = "2", title = "Note 2", updatedAt = testTimeMillis)
        
        repository.upsert(note1)
        repository.upsert(note2)
        
        val found = repository.get("1")
        assertNotNull(found, "ID로 노트를 찾을 수 있어야 함")
        assertEquals("Note 1", found.title, "올바른 노트를 찾아야 함")
        
        val notFound = repository.get("999")
        assertNull(notFound, "존재하지 않는 ID는 null을 반환해야 함")
    }

    @Test
    fun testDelete_NonExistentNote() = runTest {
        // 존재하지 않는 노트 삭제 시도
        repository.delete("999")
        
        val notes = repository.getAll()
        assertTrue(notes.isEmpty(), "존재하지 않는 노트 삭제는 영향이 없어야 함")
    }
}

