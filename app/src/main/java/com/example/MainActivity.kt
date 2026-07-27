package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.BottomNavBar
import com.example.ui.components.FullPlayerModal
import com.example.ui.components.Header
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.NavTab
import com.example.ui.components.SearchBarComponent
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.PlayTuneTheme
import com.example.ui.viewmodel.MusicViewModel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import com.example.ui.screens.ClosingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen

enum class AppLifecycleState {
    SPLASH,
    LOGIN,
    MAIN,
    CLOSING
}

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PlayTuneTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                var appState by remember { mutableStateOf(AppLifecycleState.SPLASH) }

                AnimatedContent(
                    targetState = appState,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400))) togetherWith
                                (fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.05f, animationSpec = tween(300)))
                    },
                    label = "app_lifecycle_transition"
                ) { targetState ->
                    when (targetState) {
                        AppLifecycleState.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    appState = if (isLoggedIn) AppLifecycleState.MAIN else AppLifecycleState.LOGIN
                                }
                            )
                        }

                        AppLifecycleState.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { email ->
                                    viewModel.login(email)
                                    appState = AppLifecycleState.MAIN
                                },
                                onContinueAsGuest = {
                                    viewModel.login("guest@playtune.app")
                                    appState = AppLifecycleState.MAIN
                                }
                            )
                        }

                        AppLifecycleState.MAIN -> {
                            if (!isLoggedIn) {
                                appState = AppLifecycleState.LOGIN
                            } else {
                                PlayTuneMainApp(
                                    viewModel = viewModel,
                                    onLogoutRequested = {
                                        appState = AppLifecycleState.CLOSING
                                    }
                                )
                            }
                        }

                        AppLifecycleState.CLOSING -> {
                            ClosingScreen(
                                onClosingComplete = {
                                    viewModel.logout()
                                    appState = AppLifecycleState.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayTuneMainApp(
    viewModel: MusicViewModel,
    onLogoutRequested: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) }

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val songsState by viewModel.songsState.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()

    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsStateWithLifecycle()
    val songToAddToPlaylist by viewModel.songToAddToPlaylist.collectAsStateWithLifecycle()

    // Player state from AudioPlayerManager
    val currentTrack by viewModel.audioPlayerManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioPlayerManager.isPlaying.collectAsStateWithLifecycle()
    val progressMs by viewModel.audioPlayerManager.playbackProgressMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.audioPlayerManager.trackDurationMs.collectAsStateWithLifecycle()
    val isShuffle by viewModel.audioPlayerManager.isShuffle.collectAsStateWithLifecycle()
    val isRepeat by viewModel.audioPlayerManager.isRepeat.collectAsStateWithLifecycle()
    val isLoadingTrack by viewModel.audioPlayerManager.isLoadingTrack.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            Column {
                MiniPlayerBar(
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    progressMs = progressMs,
                    durationMs = durationMs,
                    isLoading = isLoadingTrack,
                    onTogglePlayPause = { viewModel.audioPlayerManager.togglePlayPause() },
                    onNextClick = { viewModel.audioPlayerManager.nextTrack() },
                    onPrevClick = { viewModel.audioPlayerManager.previousTrack() },
                    onBarClick = { viewModel.showFullPlayer() }
                )

                BottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        },
        containerColor = BgDark,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Header(
                    modifier = Modifier.statusBarsPadding(),
                    userEmail = userEmail,
                    onFilterClick = {
                        currentTab = NavTab.SEARCH
                    },
                    onLogoutClick = {
                        onLogoutRequested()
                    }
                )

                // Search Bar shown on Home & Search screens
                if (currentTab == NavTab.HOME) {
                    SearchBarComponent(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onSearchSubmit = {
                            viewModel.executeSearch(it)
                            currentTab = NavTab.SEARCH
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(
                        targetState = currentTab,
                        label = "tab_navigation"
                    ) { tab ->
                        when (tab) {
                            NavTab.HOME -> HomeScreen(
                                selectedCategory = selectedCategory,
                                songsState = songsState,
                                favoriteIds = favoriteIds,
                                onCategorySelected = { viewModel.loadCategory(it) },
                                onSongSelected = { song, queue -> viewModel.playSong(song, queue) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onOptionClick = { viewModel.openAddToPlaylistDialog(it) },
                                onRetryClick = { viewModel.loadCategory(selectedCategory) }
                            )

                            NavTab.EXPLORE -> ExploreScreen(
                                recentHistory = recentHistory,
                                favoriteIds = favoriteIds,
                                onGenreSelected = { genre ->
                                    viewModel.loadCategory(genre)
                                    currentTab = NavTab.HOME
                                },
                                onSongSelected = { song, queue -> viewModel.playSong(song, queue) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onOptionClick = { viewModel.openAddToPlaylistDialog(it) }
                            )

                            NavTab.FAVORITES -> FavoritesScreen(
                                favoriteSongs = favoriteSongs,
                                onSongSelected = { song, queue -> viewModel.playSong(song, queue) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onOptionClick = { viewModel.openAddToPlaylistDialog(it) }
                            )

                            NavTab.PLAYLISTS -> PlaylistsScreen(
                                playlists = playlists,
                                favoriteIds = favoriteIds,
                                onCreatePlaylist = { viewModel.createPlaylist(it) },
                                onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                getSongsForPlaylist = { viewModel.getSongsForPlaylist(it) },
                                onRemoveSongFromPlaylist = { playlistId, songId ->
                                    viewModel.removeSongFromPlaylist(playlistId, songId)
                                },
                                onSongSelected = { song, queue -> viewModel.playSong(song, queue) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onOptionClick = { viewModel.openAddToPlaylistDialog(it) }
                            )

                            NavTab.SEARCH -> SearchScreen(
                                searchQuery = searchQuery,
                                songsState = songsState,
                                favoriteIds = favoriteIds,
                                onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                onSearchSubmit = { viewModel.executeSearch(it) },
                                onSongSelected = { song, queue -> viewModel.playSong(song, queue) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onOptionClick = { viewModel.openAddToPlaylistDialog(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Player Modal Bottom Sheet
    if (isFullPlayerVisible) {
        FullPlayerModal(
            song = currentTrack,
            isPlaying = isPlaying,
            progressMs = progressMs,
            durationMs = durationMs,
            isShuffle = isShuffle,
            isRepeat = isRepeat,
            isLoading = isLoadingTrack,
            onDismiss = { viewModel.hideFullPlayer() },
            onTogglePlayPause = { viewModel.audioPlayerManager.togglePlayPause() },
            onNext = { viewModel.audioPlayerManager.nextTrack() },
            onPrev = { viewModel.audioPlayerManager.previousTrack() },
            onSeek = { viewModel.audioPlayerManager.seekTo(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onToggleShuffle = { viewModel.audioPlayerManager.toggleShuffle() },
            onToggleRepeat = { viewModel.audioPlayerManager.toggleRepeat() },
            onAddToPlaylistClick = { viewModel.openAddToPlaylistDialog(it) }
        )
    }

    // Add to Playlist Dialog
    if (songToAddToPlaylist != null) {
        AddToPlaylistDialog(
            song = songToAddToPlaylist,
            playlists = playlists,
            onDismiss = { viewModel.closeAddToPlaylistDialog() },
            onSelectPlaylist = { playlistId ->
                songToAddToPlaylist?.let { song ->
                    viewModel.addSongToPlaylist(playlistId, song)
                }
            },
            onCreatePlaylist = { name ->
                viewModel.createPlaylist(name)
            }
        )
    }
}
