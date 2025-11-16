package com.example.notes

/**
 * 테스트용 헬퍼 함수
 * expect 함수를 사용하지 않고 직접 Note를 생성
 */
fun createTestNote(
    id: String = "test-id-${++testNoteIdCounter}",
    title: String = "",
    content: String = "",
    updatedAt: Long = testTimeMillis,
    deleted: Boolean = false
) = Note(id, title, content, updatedAt, deleted)

var testTimeMillis = 1000L
var testNoteIdCounter = 0

