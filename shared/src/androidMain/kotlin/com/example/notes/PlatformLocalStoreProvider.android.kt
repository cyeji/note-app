package com.example.notes

import android.content.Context

// Android actual implementation
private var androidContext: Context? = null

fun initAndroidLocalStore(context: Context) {
    androidContext = context.applicationContext
}

actual fun provideLocalStore(): LocalStore {
    val context = androidContext ?: throw IllegalStateException("Android context not initialized. Call initAndroidLocalStore() first.")
    return AndroidLocalStore(context)
}

