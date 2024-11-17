package com.cloudsbay.tasker.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MainScaffoldViewModel: ViewModel() {
    private var _showBottomBar = MutableStateFlow(true)
    var showBottomBar: StateFlow<Boolean> = _showBottomBar

    private var _showTopBar = MutableStateFlow(true)
    var showTopBar: StateFlow<Boolean> = _showTopBar

    // Function to set bottom bar visibility
    fun setShowBottomBar(isVisible: Boolean) {
        _showBottomBar.update { isVisible }
    }


    fun setShowTopBar(isVisible: Boolean) {
        _showTopBar.update { isVisible }
    }
}