package com.lumetrix.statsmanager.ui.wellness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WellnessViewModel @Inject constructor() : ViewModel() {

    private val _waterCount = MutableStateFlow(3)
    val waterCount: StateFlow<Int> = _waterCount.asStateFlow()

    private val _postureChecks = MutableStateFlow(listOf(false, false, false, false))
    val postureChecks: StateFlow<List<Boolean>> = _postureChecks.asStateFlow()

    private val _completedActivities = MutableStateFlow<Set<String>>(emptySet())
    val completedActivities: StateFlow<Set<String>> = _completedActivities.asStateFlow()

    fun incrementWaterCount() {
        viewModelScope.launch {
            _waterCount.value = (_waterCount.value + 1).coerceAtMost(8)
        }
    }

    fun togglePostureCheck(index: Int) {
        viewModelScope.launch {
            val list = _postureChecks.value.toMutableList()
            list[index] = !list[index]
            _postureChecks.value = list
        }
    }

    fun completeActivity(activityId: String) {
        viewModelScope.launch {
            _completedActivities.value = _completedActivities.value + activityId
        }
    }

    fun resetPostureChecks() {
        viewModelScope.launch {
            _postureChecks.value = listOf(false, false, false, false)
        }
    }
}
