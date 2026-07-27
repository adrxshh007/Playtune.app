package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.AccentSecondary
import com.example.ui.theme.AccentTertiary
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub

data class GenreCategory(
    val title: String,
    val query: String,
    val gradient: Brush
)

val GENRES = listOf(
    GenreCategory("Bollywood Hits", "Bollywood", Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))),
    GenreCategory("Punjabi Beats", "Punjabi Hits", Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444)))),
    GenreCategory("Lo-Fi Chill", "Lo-Fi Beats", Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))),
    GenreCategory("EDM / Club", "EDM Dance Hits", Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF06B6D4)))),
    GenreCategory("Global Pop", "Global Pop Hits", Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E)))),
    GenreCategory("Retro Classics", "Retro 90s Hits", Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))),
    GenreCategory("Acoustic Vibes", "Unplugged Acoustic", Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFB45309)))),
    GenreCategory("Rock & Metal", "Rock Hits", Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF0F172A))))
)

@Composable
fun ExploreScreen(
    recentHistory: List<Song>,
    favoriteIds: List<Long>,
    onGenreSelected: (String) -> Unit,
    onSongSelected: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOptionClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "Explore",
                    tint = AccentPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Explore Genres & Trends",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextMain
                )
            }
        }

        // Genre Grid Cards
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                items(GENRES) { genre ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(genre.gradient)
                            .clickable { onGenreSelected(genre.query) }
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = genre.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Recent Play History
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = AccentSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Recently Played Tracks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            }
        }

        if (recentHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GlassCard),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent playback history yet. Start listening!",
                        fontSize = 13.sp,
                        color = TextSub
                    )
                }
            }
        } else {
            items(recentHistory, key = { it.id }) { song ->
                val isFav = favoriteIds.contains(song.id)
                SongListItem(
                    song = song.copy(isFavorite = isFav),
                    onSongClick = { onSongSelected(song, recentHistory) },
                    onFavoriteClick = { onToggleFavorite(song) },
                    onOptionClick = { onOptionClick(song) }
                )
            }
        }
    }
}
