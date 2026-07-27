package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Song

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String,
    val previewUrl: String,
    val genre: String,
    val durationMs: Long,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            artworkUrl = artworkUrl,
            previewUrl = previewUrl,
            genre = genre,
            durationMs = durationMs,
            isFavorite = true
        )
    }

    companion object {
        fun fromSong(song: Song): FavoriteSongEntity {
            return FavoriteSongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                artworkUrl = song.artworkUrl,
                previewUrl = song.previewUrl,
                genre = song.genre,
                durationMs = song.durationMs
            )
        }
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val previewUrl: String,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song {
        return Song(
            id = songId,
            title = title,
            artist = artist,
            artworkUrl = artworkUrl,
            previewUrl = previewUrl
        )
    }
}

@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0,
    val songId: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val previewUrl: String,
    val playedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song {
        return Song(
            id = songId,
            title = title,
            artist = artist,
            artworkUrl = artworkUrl,
            previewUrl = previewUrl
        )
    }
}
