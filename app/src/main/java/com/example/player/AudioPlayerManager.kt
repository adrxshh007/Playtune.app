package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _currentTrack = MutableStateFlow<Song?>(null)
    val currentTrack: StateFlow<Song?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgressMs = MutableStateFlow(0L)
    val playbackProgressMs: StateFlow<Long> = _playbackProgressMs.asStateFlow()

    private val _trackDurationMs = MutableStateFlow(30000L)
    val trackDurationMs: StateFlow<Long> = _trackDurationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _isLoadingTrack = MutableStateFlow(false)
    val isLoadingTrack: StateFlow<Boolean> = _isLoadingTrack.asStateFlow()

    private val FALLBACK_AUDIO_STREAMS = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3"
    )

    fun playTrack(song: Song, playlistQueue: List<Song> = emptyList()) {
        if (playlistQueue.isNotEmpty()) {
            _queue.value = playlistQueue
            _currentIndex.value = playlistQueue.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }
        } else if (!_queue.value.any { it.id == song.id }) {
            _queue.value = _queue.value + song
            _currentIndex.value = _queue.value.size - 1
        }

        _currentTrack.value = song
        val streamUrl = if (song.previewUrl.isNotBlank()) {
            song.previewUrl
        } else {
            getFallbackStreamUrl(song.id)
        }
        startPlayback(streamUrl, isFallback = false, songId = song.id)
    }

    private fun getFallbackStreamUrl(songId: Long): String {
        val index = (songId.hashCode() and 0x7FFFFFFF) % FALLBACK_AUDIO_STREAMS.size
        return FALLBACK_AUDIO_STREAMS[index]
    }

    private fun startPlayback(url: String, isFallback: Boolean = false, songId: Long = 0L) {
        val targetUrl = if (url.isNotBlank()) url else getFallbackStreamUrl(songId)

        stopAndResetPlayer()
        _isLoadingTrack.value = true

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(targetUrl)
                setOnPreparedListener { mp ->
                    _isLoadingTrack.value = false
                    _trackDurationMs.value = mp.duration.toLong().coerceAtLeast(1000L)
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    stopProgressTracker()
                    handleTrackCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayerManager", "MediaPlayer error: what=$what, extra=$extra")
                    _isLoadingTrack.value = false
                    _isPlaying.value = false
                    stopProgressTracker()
                    if (!isFallback) {
                        Log.i("AudioPlayerManager", "Retrying playback with fallback stream")
                        scope.launch {
                            delay(300)
                            startPlayback(getFallbackStreamUrl(songId), isFallback = true, songId = songId)
                        }
                    }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error setting data source", e)
            _isLoadingTrack.value = false
            _isPlaying.value = false
            if (!isFallback) {
                scope.launch {
                    delay(300)
                    startPlayback(getFallbackStreamUrl(songId), isFallback = true, songId = songId)
                }
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: run {
            _currentTrack.value?.let { playTrack(it) }
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _playbackProgressMs.value = positionMs
        }
    }

    fun nextTrack() {
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return

        val nextIndex = if (_isShuffle.value) {
            (0 until currentQ.size).random()
        } else {
            (_currentIndex.value + 1) % currentQ.size
        }

        _currentIndex.value = nextIndex
        val nextSong = currentQ[nextIndex]
        playTrack(nextSong, currentQ)
    }

    fun previousTrack() {
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return

        val prevIndex = if (_currentIndex.value > 0) _currentIndex.value - 1 else currentQ.size - 1
        _currentIndex.value = prevIndex
        val prevSong = currentQ[prevIndex]
        playTrack(prevSong, currentQ)
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    private fun handleTrackCompletion() {
        if (_isRepeat.value) {
            _currentTrack.value?.let { playTrack(it) }
        } else {
            nextTrack()
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.let { player ->
                    _playbackProgressMs.value = player.currentPosition.toLong()
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun stopAndResetPlayer() {
        stopProgressTracker()
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) player.stop()
                player.reset()
                player.release()
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Error resetting player", e)
            }
        }
        mediaPlayer = null
        _isPlaying.value = false
        _playbackProgressMs.value = 0L
    }

    fun release() {
        stopAndResetPlayer()
    }
}
