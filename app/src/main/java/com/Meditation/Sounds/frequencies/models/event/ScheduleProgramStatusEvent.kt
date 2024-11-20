package com.Meditation.Sounds.frequencies.models.event

data class ScheduleProgramStatusEvent(val isPlay: Boolean = false, val isHidePlayer: Boolean = false, val isSkipQuestion: Boolean = false, val isClearScheduleProgram: Boolean = false)
