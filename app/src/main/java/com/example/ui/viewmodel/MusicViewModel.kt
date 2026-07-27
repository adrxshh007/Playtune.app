package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ITunesApiService
import com.example.data.local.PlayTuneDatabase
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("playtune_prefs", Context.MODE_PRIVATE)

    private val apiService = ITunesApiService.create()
    private val database = PlayTuneDatabase.getDatabase(application)
    private val repository = MusicRepository(apiService, database.dao())
    val audioPlayerManager = AudioPlayerManager(application)

    // Auth State
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString("user_email", "adrishh24@gmail.com") ?: "adrishh24@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    fun login(email: String) {
        val cleanEmail = email.ifBlank { "adrishh24@gmail.com" }
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", cleanEmail)
            .apply()
        _userEmail.value = cleanEmail
        _isLoggedIn.value = true
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
        _isLoggedIn.value = false
    }

    // Search & Category
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Arijit Singh")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _songsState = MutableStateFlow<UiState<List<Song>>>(UiState.Loading)
    val songsState: StateFlow<UiState<List<Song>>> = _songsState.asStateFlow()

    // Full Player Modal visibility
    private val _isFullPlayerVisible = MutableStateFlow(false)
    val isFullPlayerVisible: StateFlow<Boolean> = _isFullPlayerVisible.asStateFlow()

    // Playlist dialog for adding a song
    private val _songToAddToPlaylist = MutableStateFlow<Song?>(null)
    val songToAddToPlaylist: StateFlow<Song?> = _songToAddToPlaylist.asStateFlow()

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteIds: StateFlow<List<Long>> = repository.favoriteIds.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playlists = repository.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentHistory = repository.recentHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initial search for default category
        loadCategory("Arijit Singh")

        // Auto debounce search query
        @OptIn(FlowPreview::class)
        _searchQuery
            .debounce(400)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.trim().length >= 2) {
                    executeSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadCategory(_selectedCategory.value)
        }
    }

    fun loadCategory(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = ""
        executeSearch(category)
    }

    fun executeSearch(query: String) {
        viewModelScope.launch {
            _songsState.value = UiState.Loading
            try {
                val results = repository.searchSongs(query)
                _songsState.value = UiState.Success(results)
            } catch (e: Exception) {
                _songsState.value = UiState.Error("Failed to load music. Please check your internet connection.")
            }
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val updatedSong = song.copy(isFavorite = favoriteIds.value.contains(song.id))
        val updatedQueue = queue.map { q -> q.copy(isFavorite = favoriteIds.value.contains(q.id)) }
        audioPlayerManager.playTrack(updatedSong, updatedQueue)

        // Record in room history
        viewModelScope.launch {
            repository.recordPlay(updatedSong)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(song.id)
            repository.toggleFavorite(song.copy(isFavorite = isFav))
        }
    }

    fun showFullPlayer() {
        _isFullPlayerVisible.value = true
    }

    fun hideFullPlayer() {
        _isFullPlayerVisible.value = false
    }

    fun openAddToPlaylistDialog(song: Song) {
        _songToAddToPlaylist.value = song
    }

    fun closeAddToPlaylistDialog() {
        _songToAddToPlaylist.value = null
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name.trim())
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
            closeAddToPlaylistDialog()
        }
    }

    fun getSongsForPlaylist(playlistId: Long) = repository.getSongsForPlaylist(playlistId)

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
    }
}
