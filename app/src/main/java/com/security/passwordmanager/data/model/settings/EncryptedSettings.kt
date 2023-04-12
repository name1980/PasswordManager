package com.security.passwordmanager.data.model.settings

import com.security.passwordmanager.presentation.model.Time
import com.security.passwordmanager.presentation.model.enums.ColorDesign

data class EncryptedSettings(
    val colorDesign: String = ColorDesign.System.toString(),
    val sunriseTime: String = Time.defaultSunriseTime.toString(),
    val sunsetTime: String = Time.defaultSunsetTime.toString(),
    val beautifulFont: String = "true",
    val autofill: String = "true",
    val dynamicColor: String = "false",
    val pullToRefresh: String = "true",
)
