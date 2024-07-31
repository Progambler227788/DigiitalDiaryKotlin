package com.diary.digitaldiary.models

data class DiaryEntry(
    val id: Long,
    val title: String,
    val location: String,
    val note: String,
    val photoPath: String?,
    val voicePath: String?
)
