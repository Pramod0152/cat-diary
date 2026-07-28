package com.purrcare.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.purrcare.PurrCareApplication
import com.purrcare.data.entity.CatProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val catDao = (application as PurrCareApplication).database.catDao()

    val allCats: StateFlow<List<CatProfile>> = catDao.getAllCats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCatId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCat: StateFlow<CatProfile?> = _selectedCatId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                catDao.getCatById(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _profileSaveState = MutableStateFlow<ProfileSaveState>(ProfileSaveState.Idle)
    val profileSaveState: StateFlow<ProfileSaveState> = _profileSaveState.asStateFlow()

    init {
        viewModelScope.launch {
            val cats = catDao.getAllCats()
            cats.collect { profiles ->
                if (profiles.isNotEmpty() && _selectedCatId.value == null) {
                    selectCat(profiles.first().id)
                }
            }
        }
    }

    fun selectCat(catId: Long) {
        _selectedCatId.value = catId
        viewModelScope.launch {
            catDao.getCatByIdOnce(catId)?.let { cat ->
                _profileSaveState.value = ProfileSaveState.Loaded(cat)
            }
        }
    }

    fun createOrUpdateProfile(
        profile: CatProfile,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            _profileSaveState.value = ProfileSaveState.Saving
            try {
                val id = if (profile.id == 0L) {
                    catDao.insert(profile)
                } else {
                    catDao.update(profile)
                    profile.id
                }
                _selectedCatId.value = id
                _profileSaveState.value = ProfileSaveState.Saved
                onSaved(id)
            } catch (e: Exception) {
                _profileSaveState.value = ProfileSaveState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetSaveState() {
        _profileSaveState.value = ProfileSaveState.Idle
    }
}

sealed class ProfileSaveState {
    data object Idle : ProfileSaveState()
    data object Saving : ProfileSaveState()
    data object Saved : ProfileSaveState()
    data class Loaded(val profile: CatProfile) : ProfileSaveState()
    data class Error(val message: String) : ProfileSaveState()
}
