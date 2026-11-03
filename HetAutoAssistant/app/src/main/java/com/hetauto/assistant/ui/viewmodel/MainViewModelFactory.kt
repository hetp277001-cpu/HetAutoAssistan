package com.hetauto.assistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hetauto.assistant.HetApplication

class MainViewModelFactory(private val application: HetApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                repository = application.repository,
                loopController = application.loopController,
                screenshotManager = application.screenshotManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
