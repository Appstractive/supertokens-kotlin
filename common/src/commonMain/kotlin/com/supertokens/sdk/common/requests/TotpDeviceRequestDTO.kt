package com.supertokens.sdk.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class TotpDeviceRequestDTO(
    val deviceName: String,
)
