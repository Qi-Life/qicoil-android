package com.Meditation.Sounds.frequencies.models

import com.google.android.exoplayer2.source.MediaSource
import java.io.Serializable

class ScalarMediaSource(
    var tag: String = "",
    var mediaSource: MediaSource? = null
) : Serializable {

}