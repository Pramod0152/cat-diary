package com.petwell.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petwell.PetWellApplication
import com.petwell.data.entity.PetProfile
import com.petwell.data.entity.enums.Species
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val petDao = (application as PetWellApplication).database.petDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allPets: StateFlow<List<PetProfile>> = petDao.getAllPets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPetId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPet: StateFlow<PetProfile?> = _selectedPetId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else petDao.getPetById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _saveState = MutableStateFlow<PetSaveState>(PetSaveState.Idle)
    val saveState: StateFlow<PetSaveState> = _saveState.asStateFlow()

    init {
        viewModelScope.launch {
            petDao.getAllPets().collect { profiles ->
                if (profiles.isNotEmpty() && _selectedPetId.value == null) {
                    selectPet(profiles.first().id)
                }
            }
        }
    }

    fun selectPet(petId: Long) {
        _selectedPetId.value = petId
    }

    fun createOrUpdateProfile(
        profile: PetProfile,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            _saveState.value = PetSaveState.Saving
            try {
                val id = if (profile.id == 0L) {
                    petDao.insert(profile)
                } else {
                    petDao.update(profile)
                    profile.id
                }
                _selectedPetId.value = id
                _saveState.value = PetSaveState.Saved
                onSaved(id)
            } catch (e: Exception) {
                _saveState.value = PetSaveState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = PetSaveState.Idle
    }
}

sealed class PetSaveState {
    data object Idle : PetSaveState()
    data object Saving : PetSaveState()
    data object Saved : PetSaveState()
    data class Error(val message: String) : PetSaveState()
}
