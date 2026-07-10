package com.lumetrix.statsmanager.ui.appdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumetrix.statsmanager.data.repository.UsageTrackingRepository
import com.lumetrix.statsmanager.domain.model.AppDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    private val repository: UsageTrackingRepository,
) : ViewModel() {

    private val packageNameFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AppDetailsUiState> = packageNameFlow
        .flatMapLatest { pkg ->
            if (pkg == null) {
                kotlinx.coroutines.flow.flowOf(AppDetailsUiState(isLoading = true))
            } else {
                repository.observeAppDetailsState(pkg)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppDetailsUiState(isLoading = true)
        )

    fun loadAppDetails(packageName: String) {
        packageNameFlow.value = packageName
    }

    fun updateCategory(category: com.lumetrix.statsmanager.domain.model.AppCategory) {
        val pkg = packageNameFlow.value ?: return
        viewModelScope.launch {
            repository.setAppCategory(pkg, category)
        }
    }
}
