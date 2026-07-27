package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ITunesSearchResponse(
    @Json(name = "resultCount") val resultCount: Int = 0,
    @Json(name = "results") val results: List<ITunesSongDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ITunesSongDto(
    @Json(name = "trackId") val trackId: Long? = null,
    @Json(name = "trackName") val trackName: String? = null,
    @Json(name = "artistName") val artistName: String? = null,
    @Json(name = "collectionName") val collectionName: String? = null,
    @Json(name = "artworkUrl100") val artworkUrl100: String? = null,
    @Json(name = "previewUrl") val previewUrl: String? = null,
    @Json(name = "primaryGenreName") val primaryGenreName: String? = null,
    @Json(name = "trackTimeMillis") val trackTimeMillis: Long? = null
)

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String = "",
    val artworkUrl: String = "",
    val previewUrl: String = "",
    val genre: String = "Music",
    val durationMs: Long = 30000L,
    val isFavorite: Boolean = false
) {
    fun getHighResArtwork(): String {
        return if (artworkUrl.isNotEmpty()) {
            artworkUrl.replace("100x100bb", "600x600bb")
                .replace("100x100", "600x600")
        } else {
            ""
        }
    }
}

fun ITunesSongDto.toSong(isFavorite: Boolean = false): Song? {
    val validId = trackId ?: return null
    val validTitle = trackName ?: return null
    val validArtist = artistName ?: "Unknown Artist"
    val validPreview = if (!previewUrl.isNullOrEmpty()) {
        previewUrl
    } else {
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(validId % 10 + 1)}.mp3"
    }

    return Song(
        id = validId,
        title = validTitle,
        artist = validArtist,
        album = collectionName ?: "",
        artworkUrl = artworkUrl100 ?: "",
        previewUrl = validPreview,
        genre = primaryGenreName ?: "Music",
        durationMs = trackTimeMillis ?: 30000L,
        isFavorite = isFavorite
    )
}
