package com.example.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope

// actual implementation for provideLocalStore
actual fun provideLocalStore(): LocalStore = FileLocalStore()

// helper to create repository and sync manager singletons for desktop
object DesktopServices {
    val localStore: LocalStore by lazy { provideLocalStore() }
    val repo: NoteRepository by lazy { NoteRepository(localStore) }
    val server: FakeServer by lazy { FakeServer() }
    val syncManager: SyncManager by lazy { SyncManager(repo, server) }
}

@Composable
fun MainNotesView() {
    val scope = rememberCoroutineScope()
    NotesScreen(DesktopServices.repo, DesktopServices.syncManager, scope)
}

