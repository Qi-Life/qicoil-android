package com.Meditation.Sounds.frequencies.models

data class ProgramSchedule(val programId: Int? = -1, val programName: String? = null, val startTimeAm: Float, val stopTimeAm: Float, val startTimePm: Float, val stopTimePm: Float)
