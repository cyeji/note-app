package com.example.notes

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class FakeServerTest {
    private lateinit var server: FakeServer

    @BeforeTest
    fun setup() {
        server = FakeServer()
        testNoteIdCounter = 0
        testTimeMillis = 1000L
    }

    @Test
    fun testLoadAll_Empty() = runTest {
        val notes = server.loadAll()
        assertTrue(notes.isEmpty(), "초기 상태에서는 빈 리스트여야 함")
    }

    @Test
    fun testUpsert_AddNew() = runTest {
        val note = createTestNote(id = "1", title = "Test", updatedAt = testTimeMillis)
        server.upsert(note)
        
        val notes = server.loadAll()
        assertEquals(1, notes.size, "노트가 1개 추가되어야 함")
        assertEquals("Test", notes[0].title, "제목이 일치해야 함")
    }

    @Test
    fun testUpsert_LastWriteWins() = runTest {
        val older = createTestNote(id = "1", title = "Older", updatedAt = 1000L)
        val newer = createTestNote(id = "1", title = "Newer", updatedAt = 2000L)
        
        server.upsert(older)
        server.upsert(newer)
        
        val notes = server.loadAll()
        assertEquals(1, notes.size, "노트가 1개여야 함 (업데이트)")
        assertEquals("Newer", notes[0].title, "더 최신 노트가 저장되어야 함 (LWW)")
    }

    @Test
    fun testUpsert_OlderNoteNotOverwritten() = runTest {
        val newer = createTestNote(id = "1", title = "Newer", updatedAt = 2000L)
        val older = createTestNote(id = "1", title = "Older", updatedAt = 1000L)
        
        server.upsert(newer)
        server.upsert(older) // 더 오래된 노트는 덮어쓰지 않아야 함
        
        val notes = server.loadAll()
        assertEquals(1, notes.size)
        assertEquals("Newer", notes[0].title, "더 최신 노트가 유지되어야 함")
    }

    @Test
    fun testClear() = runTest {
        server.upsert(createTestNote(id = "1", title = "Test", updatedAt = testTimeMillis))
        server.clear()
        
        val notes = server.loadAll()
        assertTrue(notes.isEmpty(), "clear 후에는 빈 리스트여야 함")
    }

    @Test
    fun testReplaceAll() = runTest {
        server.upsert(createTestNote(id = "1", title = "First", updatedAt = testTimeMillis))
        server.upsert(createTestNote(id = "2", title = "Second", updatedAt = testTimeMillis))
        
        val newNotes = listOf(
            createTestNote(id = "3", title = "Third", updatedAt = testTimeMillis),
            createTestNote(id = "4", title = "Fourth", updatedAt = testTimeMillis)
        )
        server.replaceAll(newNotes)
        
        val notes = server.loadAll()
        assertEquals(2, notes.size, "replaceAll 후에는 새로운 노트만 있어야 함")
        assertTrue(notes.any { it.id == "3" }, "새 노트가 포함되어야 함")
        assertTrue(notes.any { it.id == "4" }, "새 노트가 포함되어야 함")
        assertFalse(notes.any { it.id == "1" }, "기존 노트는 제거되어야 함")
        assertFalse(notes.any { it.id == "2" }, "기존 노트는 제거되어야 함")
    }

    @Test
    fun testMultipleNotes() = runTest {
        server.upsert(createTestNote(id = "1", title = "One", updatedAt = testTimeMillis))
        server.upsert(createTestNote(id = "2", title = "Two", updatedAt = testTimeMillis))
        server.upsert(createTestNote(id = "3", title = "Three", updatedAt = testTimeMillis))
        
        val notes = server.loadAll()
        assertEquals(3, notes.size, "노트가 3개 있어야 함")
    }
}

