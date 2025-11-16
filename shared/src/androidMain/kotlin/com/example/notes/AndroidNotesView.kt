package com.example.notes

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope

// Android services singleton
object AndroidServices {
    private var _localStore: LocalStore? = null
    private var _repo: NoteRepository? = null
    private var _server: FakeServer? = null
    private var _syncManager: SyncManager? = null

    fun init(context: Context) {
        initAndroidLocalStore(context)
        _localStore = provideLocalStore()
        _repo = NoteRepository(_localStore!!)
        _server = FakeServer()
        _syncManager = SyncManager(_repo!!, _server!!)
    }

    val repo: NoteRepository
        get() = _repo ?: throw IllegalStateException("AndroidServices not initialized")
    val syncManager: SyncManager
        get() = _syncManager ?: throw IllegalStateException("AndroidServices not initialized")
}

@Composable
fun MainNotesView() {
    val scope = rememberCoroutineScope()
    NotesScreen(AndroidServices.repo, AndroidServices.syncManager, scope)
}

