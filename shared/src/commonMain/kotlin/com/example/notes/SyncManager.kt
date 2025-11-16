package com.example.notes

class SyncManager(private val repo: NoteRepository, private val server: FakeServer) {
    suspend fun push() {
        val local = repo.getAll()
        val serverAll = server.loadAll()
        val serverMap = serverAll.associateBy { it.id }
        local.forEach { note ->
            val serverNote = serverMap[note.id]
            if (serverNote == null || note.updatedAt >= serverNote.updatedAt) {
                server.upsert(note)
            }
        }
    }

    suspend fun pull() {
        val serverNotes = server.loadAll()
        serverNotes.forEach { srv ->
            val local = repo.get(srv.id)
            if (local == null || srv.updatedAt >= local.updatedAt) {
                repo.upsert(srv)
            }
        }
    }

    suspend fun syncBothWays() {
        // 단순한 양방향: 먼저 pull, 그 다음 push
        pull()
        push()
    }
}

