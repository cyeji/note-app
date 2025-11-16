package com.example.notes

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(repo: NoteRepository, syncManager: SyncManager, scope: CoroutineScope) {
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var editing by remember { mutableStateOf<Note?>(null) }

    LaunchedEffect(Unit) {
        repo.init()
        notes = repo.getAll()
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Notes", modifier = Modifier.padding(8.dp))
                Button(onClick = {
                    scope.launch {
                        syncManager.syncBothWays()
                        notes = repo.getAll()
                    }
                }) {
                    Text("Sync")
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                notes.forEach { note ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editing = note
                        }
                        .padding(8.dp)
                    ) {
                        Column {
                            Text(note.title.ifBlank { "(no title)" })
                            Text("Updated: ${note.updatedAt}")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            editing?.let { n ->
                Column {
                    BasicTextField(value = n.title, onValueChange = { new ->
                        editing = n.copy(title = new, updatedAt = currentTimeMillis())
                    })
                    Spacer(Modifier.height(4.dp))
                    BasicTextField(value = n.content, onValueChange = { new ->
                        editing = n.copy(content = new, updatedAt = currentTimeMillis())
                    })
                    Row {
                        Button(onClick = {
                            scope.launch {
                                editing?.let { repo.upsert(it) }
                                notes = repo.getAll()
                                editing = null
                            }
                        }) { Text("Save") }

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = {
                            scope.launch {
                                repo.delete(n.id)
                                notes = repo.getAll()
                                editing = null
                            }
                        }) { Text("Delete") }
                    }
                }
            } ?: Button(onClick = {
                scope.launch {
                    val new = createNote(title = "New note", content = "")
                    repo.upsert(new)
                    notes = repo.getAll()
                }
            }) {
                Text("Add Note")
            }
        }
    }
}

