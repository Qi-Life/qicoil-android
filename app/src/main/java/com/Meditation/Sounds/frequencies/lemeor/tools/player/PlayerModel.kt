package com.Meditation.Sounds.frequencies.lemeor.tools.player

data class PlayerShuffle(val it: Boolean)
data class PlayerRepeat(val type: Int)
data class PlayerSeek(val position: Int)
data class PlayerSelected(val position: Int?)
data class PlayerStatus(val isPlaying: Boolean = false, val isPause: Boolean = false)
data class ScalarPlayerStatus(val isPlaying: Boolean = true)
data class PlayerPlayAction(val isLastPlaying: Boolean = false)