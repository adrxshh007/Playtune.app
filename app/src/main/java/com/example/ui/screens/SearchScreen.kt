package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.SearchBarComponent
import com.example.ui.components.SongListItem
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.viewmodel.UiState

val POPULAR_SEARCH_KEYWORDS = listOf(
    "Arijit Singh", "Taylor Swift", "Pritam", "The Weeknd",
    "Bollywood Romantic", "Coldplay", "Lo-Fi Instrumental", "Ed Sheeran", "Shreya Ghoshal"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    songsState: UiState<List<Song>>,
    favoriteIds: List<Long>,
    onQueryChanged: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOptionClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SearchBarComponent(
            query = searchQuery,
            onQueryChange = onQueryChanged,
            onSearchSubmit = onSearchSubmit
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (searchQuery.isBlank()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trending",
                            tint = AccentPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trending Searches",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    }
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        POPULAR_SEARCH_KEYWORDS.forEach { keyword ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GlassCard)
                                    .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                                    .clickable {
                                        onQueryChanged(keyword)
                                        onSearchSubmit(keyword)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = TextSub,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = keyword,
                                        fontSize = 12.sp,
                                        color = TextMain
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Search Results for \"$searchQuery\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }

                item {
                    AnimatedContent(
                        targetState = songsState,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(280)) + slideInVertically(animationSpec = tween(280)) { it / 10 }) togetherWith
                                    fadeOut(animationSpec = tween(180))
                        },
                        label = "SearchResultsTransition"
                    ) { currentState ->
                        when (currentState) {
                            is UiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AccentPrimary)
                                }
                            }

                            is UiState.Error -> {
                                Text(
                                    text = currentState.message,
                                    color = TextSub,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 20.dp)
                                )
                            }

                            is UiState.Success -> {
                                val songs = currentState.data
                                if (songs.isEmpty()) {
                                    Text(
                                        text = "No songs matching \"$searchQuery\"",
                                        color = TextSub,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 20.dp)
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        songs.forEach { song ->
                                            val isFav = favoriteIds.contains(song.id)
                                            SongListItem(
                                                song = song.copy(isFavorite = isFav),
                                                onSongClick = { onSongSelected(song, songs) },
                                                onFavoriteClick = { onToggleFavorite(song) },
                                                onOptionClick = { onOptionClick(song) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
