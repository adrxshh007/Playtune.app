package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.CategoryChips
import com.example.ui.components.HeroSpotlight
import com.example.ui.components.SongCard
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.AccentSecondary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    selectedCategory: String,
    songsState: UiState<List<Song>>,
    favoriteIds: List<Long>,
    onCategorySelected: (String) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOptionClick: (Song) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        val currentCategorySongs = (songsState as? UiState.Success)?.data ?: emptyList()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Hero Spotlight Header
            item(span = { GridItemSpan(2) }) {
                HeroSpotlight(
                    artistName = if (selectedCategory.isNotBlank()) selectedCategory else "Arijit Singh",
                    subtitle = "Listen to popular songs without downloading.",
                    onPlayAllClick = {
                        if (currentCategorySongs.isNotEmpty()) {
                            onSongSelected(currentCategorySongs.first(), currentCategorySongs)
                        }
                    }
                )
            }

            // Quick Category Filter Chips
            item(span = { GridItemSpan(2) }) {
                CategoryChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            // Section Title
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "$selectedCategory Songs",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // State Handling (Loading / Error / Content)
            when (songsState) {
                is UiState.Loading -> {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = AccentPrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Loading music...",
                                    color = TextSub,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = songsState.message,
                                    color = TextSub,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onRetryClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry"
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text("Try Again")
                                }
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val songs = songsState.data
                    if (songs.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No songs found for \"$selectedCategory\"",
                                    color = TextSub,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(songs, key = { it.id }) { song ->
                            val isFav = favoriteIds.contains(song.id)
                            SongCard(
                                song = song.copy(isFavorite = isFav),
                                onSongClick = { onSongSelected(song, songs) },
                                onFavoriteClick = { onToggleFavorite(song) },
                                onOptionClick = { onOptionClick(song) }
                            )
                        }
                    }
                }
            }

            // Developer Footer
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Developed by adrxshh",
                    fontSize = 11.sp,
                    color = TextSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
