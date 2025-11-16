package com.example.notes

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SyncManagerTest {
    private lateinit var mockStore: MockLocalStore
    private lateinit var repository: NoteRepository
    private lateinit var server: FakeServer
    private lateinit var syncManager: SyncManager

    @BeforeTest
    fun setup() {
        mockStore = MockLocalStore()
        repository = NoteRepository(mockStore)
        server = FakeServer()
        syncManager = SyncManager(repository, server)
        testNoteIdCounter = 0
        testTimeMillis = 1000L
    }

    @Test
    fun testPush_LocalToServer() = runTest {
        val note = createTestNote(id = "1", title = "Local Note", updatedAt = 1000L)
        repository.upsert(note)
        
        syncManager.push()
        
        val serverNotes = server.loadAll()
        assertEquals(1, serverNotes.size, "서버에 노트가 1개 있어야 함")
        assertEquals("Local Note", serverNotes[0].title, "서버의 노트 제목이 일치해야 함")
    }

    @Test
    fun testPull_ServerToLocal() = runTest {
        val note = createTestNote(id = "1", title = "Server Note", updatedAt = 1000L)
        server.upsert(note)
        
        syncManager.pull()
        
        val localNotes = repository.getAll()
        assertEquals(1, localNotes.size, "로컬에 노트가 1개 있어야 함")
        assertEquals("Server Note", localNotes[0].title, "로컬의 노트 제목이 일치해야 함")
    }

    @Test
    fun testSyncBothWays_MergeNotes() = runTest {
        // 로컬에 노트 추가
        val localNote = createTestNote(id = "1", title = "Local", updatedAt = 1000L)
        repository.upsert(localNote)
        
        // 서버에 다른 노트 추가
        val serverNote = createTestNote(id = "2", title = "Server", updatedAt = 2000L)
        server.upsert(serverNote)
        
        syncManager.syncBothWays()
        
        val localNotes = repository.getAll()
        val serverNotes = server.loadAll()
        
        assertEquals(2, localNotes.size, "로컬에 2개 노트가 있어야 함")
        assertEquals(2, serverNotes.size, "서버에 2개 노트가 있어야 함")
    }

    @Test
    fun testPush_LastWriteWins() = runTest {
        val olderNote = createTestNote(id = "1", title = "Older", updatedAt = 1000L)
        val newerNote = createTestNote(id = "1", title = "Newer", updatedAt = 2000L)
        
        server.upsert(olderNote)
        repository.upsert(newerNote)
        
        syncManager.push()
        
        val serverNotes = server.loadAll()
        assertEquals(1, serverNotes.size, "서버에 노트가 1개여야 함")
        assertEquals("Newer", serverNotes[0].title, "더 최신 노트가 서버에 저장되어야 함 (LWW)")
    }

    @Test
    fun testPull_LastWriteWins() = runTest {
        val olderNote = createTestNote(id = "1", title = "Older", updatedAt = 1000L)
        val newerNote = createTestNote(id = "1", title = "Newer", updatedAt = 2000L)
        
        repository.upsert(olderNote)
        server.upsert(newerNote)
        
        syncManager.pull()
        
        val localNotes = repository.getAll()
        assertEquals(1, localNotes.size, "로컬에 노트가 1개여야 함")
        assertEquals("Newer", localNotes[0].title, "더 최신 노트가 로컬에 저장되어야 함 (LWW)")
    }

    @Test
    fun testSyncBothWays_ConflictResolution() = runTest {
        // 같은 ID, 다른 내용, 다른 시간
        val localNote = createTestNote(id = "1", title = "Local Title", content = "Local", updatedAt = 1500L)
        val serverNote = createTestNote(id = "1", title = "Server Title", content = "Server", updatedAt = 2000L)
        
        repository.upsert(localNote)
        server.upsert(serverNote)
        
        syncManager.syncBothWays()
        
        // Pull 후 Push이므로 서버의 최신 버전이 로컬에 반영되고, 그게 다시 서버로 push됨
        val localNotes = repository.getAll()
        val serverNotes = server.loadAll()
        
        assertEquals(1, localNotes.size)
        assertEquals(1, serverNotes.size)
        // 서버가 더 최신이므로 서버 버전이 최종적으로 반영되어야 함
        assertEquals("Server Title", serverNotes[0].title)
    }

    @Test
    fun testSyncBothWays_EmptyBoth() = runTest {
        // 빈 상태에서 동기화
        syncManager.syncBothWays()
        
        val localNotes = repository.getAll()
        val serverNotes = server.loadAll()
        
        assertTrue(localNotes.isEmpty(), "로컬이 비어있어야 함")
        assertTrue(serverNotes.isEmpty(), "서버가 비어있어야 함")
    }
}

