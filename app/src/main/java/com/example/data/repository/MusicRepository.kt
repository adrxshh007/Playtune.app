package com.example.data.repository

import com.example.data.api.ITunesApiService
import com.example.data.local.FavoriteSongEntity
import com.example.data.local.PlayHistoryEntity
import com.example.data.local.PlayTuneDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongEntity
import com.example.data.model.Song
import com.example.data.model.toSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val apiService: ITunesApiService,
    private val dao: PlayTuneDao
) {

    suspend fun searchSongs(query: String): List<Song> {
        return try {
            val response = apiService.searchSongs(term = query, limit = 50)
            val songs = response.results.mapNotNull { it.toSong() }
            if (songs.isNotEmpty()) {
                songs
            } else {
                getCuratedSongsForQuery(query)
            }
        } catch (e: Exception) {
            getCuratedSongsForQuery(query)
        }
    }

    private fun getCuratedSongsForQuery(query: String): List<Song> {
        val q = query.lowercase()
        return CURATED_FALLBACK_SONGS.filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.genre.lowercase().contains(q) ||
            q.contains(it.artist.lowercase()) ||
            q.contains(it.genre.lowercase())
        }.ifEmpty {
            CURATED_FALLBACK_SONGS
        }
    }

    companion object {
        val CURATED_FALLBACK_SONGS = listOf(
            Song(
                id = 1001L,
                title = "Kesariya",
                artist = "Arijit Singh, Pritam",
                album = "Brahmastra",
                artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                genre = "Bollywood",
                durationMs = 180000L
            ),
            Song(
                id = 1002L,
                title = "Tum Hi Ho",
                artist = "Arijit Singh",
                album = "Aashiqui 2",
                artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80",
                previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                genre = "Bollywood",
                durationMs = 262000L
            ),
            Song(
                id = 1003L,
                title = "Tere Hawaale",
                artist = "Arijit Singh & Shilpa Rao",
                album = "Laal Singh Chaddha",
                artworkUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&q=80",
                previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                genre = "Bollywood",
                durationMs = 346000L
            ),
            Song(
                id = 1004L,
                title = "Pehli Dafa",
                artist = "Atif Aslam",
                album = "Single",
                artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                genre = "Atif Aslam",
                durationMs = 280000L
            ),
            Song(
                id = 1005L,
                title = "Softly",
                artist = "Karan Aujla",
                album = "Making Memories",
                artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80",
                previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                genre = "Punjabi Hits",
                durationMs = 160000L
            ),
            Song(
                id = 1006L,
                title = "Midnight Chill",
                artist = "Lo-Fi Collective",
                album = "Study & Relax",
                artworkUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&q=80",
                previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                genre = "Lo-Fi Beats",
                durationMs = 190000L
            ),
            Song(
                id = 1007L,
                title = "Neon Dreams",
                artist = "Synthwave Beats",
                album = "Retro Glow",
                artworkUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=500&q=80",
                previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3",
                genre = "EDM Party",
                durationMs = 210000L
            ),
            Song(
                id = 1008L,
                title = "Rockstar Vibes",
                artist = "Electric Echoes",
                album = "Stadium Anthems",
                artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&q=80",
                previewUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                genre = "Rock Hits",
                durationMs = 230000L
            )
        )
    }

    val favoriteIds: Flow<List<Long>> = dao.getAllFavoriteIds()

    val favoriteSongs: Flow<List<Song>> = dao.getAllFavorites().map { entities ->
        entities.map { it.toSong() }
    }

    suspend fun toggleFavorite(song: Song) {
        val entity = FavoriteSongEntity.fromSong(song)
        if (song.isFavorite) {
            dao.removeFavorite(song.id)
        } else {
            dao.insertFavorite(entity)
        }
    }

    suspend fun removeFavorite(songId: Long) {
        dao.removeFavorite(songId)
    }

    // Playlists
    val playlists: Flow<List<PlaylistEntity>> = dao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return dao.createPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        val entity = PlaylistSongEntity(
            playlistId = playlistId,
            songId = song.id,
            title = song.title,
            artist = song.artist,
            artworkUrl = song.artworkUrl,
            previewUrl = song.previewUrl
        )
        dao.addSongToPlaylist(entity)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return dao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toSong() }
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }

    // Play history
    suspend fun recordPlay(song: Song) {
        val history = PlayHistoryEntity(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            artworkUrl = song.artworkUrl,
            previewUrl = song.previewUrl
        )
        dao.recordPlayHistory(history)
    }

    val recentHistory: Flow<List<Song>> = dao.getRecentHistory().map { entities ->
        // Distinct by songId to show unique recent tracks
        entities.distinctBy { it.songId }.map { it.toSong() }
    }
}
