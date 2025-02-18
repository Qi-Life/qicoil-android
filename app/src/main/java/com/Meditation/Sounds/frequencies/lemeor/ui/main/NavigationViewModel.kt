package com.Meditation.Sounds.frequencies.lemeor.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class NavigationViewModel : ViewModel() {
    private var _navigationIsCollapse = MutableStateFlow(false)
    val navigationIsCollapse : MutableStateFlow<Boolean> = _navigationIsCollapse

    fun onTabNavigationCollapse(){
        _navigationIsCollapse.update { !_navigationIsCollapse.value }
    }

}