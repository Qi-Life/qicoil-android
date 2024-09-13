package com.Meditation.Sounds.frequencies.lemeor.data.model

data class ScalarSubscriptionResponse(
    val message: String, val data: ScalarSubscription
)

data class ScalarSubscription(
    val is_scalar_unlocked: Int,
    val payment_url: String,
)