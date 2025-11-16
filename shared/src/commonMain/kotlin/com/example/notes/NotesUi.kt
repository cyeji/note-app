package com.example.notes

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            repo.init()
            notes = repo.getAll()
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "초기화 오류: ${e.message}"
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Notes", modifier = Modifier.padding(8.dp))
                Button(onClick = {
                    scope.launch {
                        try {
                            errorMessage = null
                            syncManager.syncBothWays()
                            notes = repo.getAll()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorMessage = "동기화 오류: ${e.message}"
                        }
                    }
                }) {
                    Text("Sync")
                }
            }

            // 에러 메시지 표시
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = androidx.compose.material.MaterialTheme.colors.error,
                    modifier = Modifier.padding(8.dp)
                )
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
                var titleText by remember(n.id) { mutableStateOf(n.title) }
                var contentText by remember(n.id) { mutableStateOf(n.content) }
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("제목") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        label = { Text("내용") },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        maxLines = 10
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            scope.launch {
                                try {
                                    errorMessage = null
                                    val updatedNote = n.copy(
                                        title = titleText,
                                        content = contentText,
                                        updatedAt = currentTimeMillis()
                                    )
                                    repo.upsert(updatedNote)
                                    notes = repo.getAll()
                                    editing = null
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorMessage = "저장 오류: ${e.message}"
                                }
                            }
                        }) { Text("Save") }

                        Spacer(Modifier.width(8.dp))

                        Button(onClick = {
                            scope.launch {
                                try {
                                    errorMessage = null
                                    repo.delete(n.id)
                                    notes = repo.getAll()
                                    editing = null
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorMessage = "삭제 오류: ${e.message}"
                                }
                            }
                        }) { Text("Delete") }
                        
                        Spacer(Modifier.width(8.dp))
                        
                        Button(onClick = {
                            editing = null
                        }) { Text("취소") }
                    }
                }
            } ?: Button(onClick = {
                scope.launch {
                    try {
                        errorMessage = null
                        val new = createNote(title = "", content = "")
                        repo.upsert(new)
                        notes = repo.getAll()
                        // 새 노트를 바로 편집 모드로 전환
                        editing = new
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "노트 추가 오류: ${e.message}"
                    }
                }
            }) {
                Text("Add Note")
            }
        }
    }
}

