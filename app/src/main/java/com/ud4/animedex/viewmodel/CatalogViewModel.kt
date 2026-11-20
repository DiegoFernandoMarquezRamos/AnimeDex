package com.ud4.animedex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ud4.animedex.data.dao.AnimeDao
import com.ud4.animedex.data.entity.AnimeEntity
import com.ud4.animedex.data.repository.SettingsRepository
import com.ud4.animedex.data.repository.SortOrder
import com.ud4.animedex.data.repository.UserSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CatalogViewModel(
    private val dao: AnimeDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val query = MutableStateFlow("")

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            UserSettings(SortOrder.RANK, 0f, false, emptySet())
        )

    val animes: StateFlow<List<AnimeEntity>> = combine(
        query.debounce(300),
        settingsRepository.settings
    ) { q, s -> Pair(q, s) }
        .flatMapLatest { (q, s) ->
            dao.search(
                text = q,
                minScore = s.minScore,
                sort = s.sort.name
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteAnimes: StateFlow<List<AnimeEntity>> = settingsRepository.settings
        .map { it.favorites }
        .distinctUntilChanged()
        .flatMapLatest { favs ->
            if (favs.isEmpty()) {
                flowOf(emptyList())
            } else {
                dao.byIds(favs.toList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun toggleFavorite(animeId: Int) {
        viewModelScope.launch {
            settingsRepository.toggleFavorite(animeId)
        }
    }

    fun setSort(sort: SortOrder) {
        viewModelScope.launch {
            settingsRepository.setSort(sort)
        }
    }

    fun setMinScore(score: Float) {
        viewModelScope.launch {
            settingsRepository.setMinScore(score)
        }
    }

    fun setDarkTheme(dark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(dark)
        }
    }
}
