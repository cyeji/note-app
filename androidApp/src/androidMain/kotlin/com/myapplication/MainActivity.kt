package com.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.notes.AndroidServices
import com.example.notes.MainNotesView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Android services with context
        AndroidServices.init(this)

        setContent {
            MainNotesView()
        }
    }
}