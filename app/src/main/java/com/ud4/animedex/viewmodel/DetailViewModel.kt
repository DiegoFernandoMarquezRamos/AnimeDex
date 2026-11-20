package com.ud4.animedex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ud4.animedex.data.dao.AnimeDao
import com.ud4.animedex.data.dao.AnimeWithGenres
import com.ud4.animedex.data.dao.CharacterWithVoice
import com.ud4.animedex.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    val animeId: Int,
    private val dao: AnimeDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val animeDetail: StateFlow<AnimeWithGenres?> = dao.detail(animeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val studio: StateFlow<String?> = dao.studio(animeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val characters: StateFlow<List<CharacterWithVoice>> = dao.mainCharactersWithVoice(animeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isFavorite: StateFlow<Boolean> = settingsRepository.settings
        .map { it.favorites.contains(animeId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleFavorite() {
        viewModelScope.launch {
            settingsRepository.toggleFavorite(animeId)
        }
    }
}
