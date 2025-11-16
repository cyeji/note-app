package com.example.notes

import androidx.compose.runtime.Composable
import kotlinx.coroutines.MainScope

// Desktop actual implementation
actual fun provideLocalStore(): LocalStore = FileLocalStore()

// helper singletons for desktop
object DesktopServices {
    val localStore: LocalStore by lazy { provideLocalStore() }
    val repo: NoteRepository by lazy { NoteRepository(localStore) }
    val server: FakeServer by lazy { FakeServer() }
    val syncManager: SyncManager by lazy { SyncManager(repo, server) }
}

@Composable
fun MainNotesView() {
    val scope = MainScope()
    NotesScreen(DesktopServices.repo, DesktopServices.syncManager, scope)
}

