package com.antoniowalls.ava.ui.theme

import com.antoniowalls.ava.design.UiDimensions
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.antoniowalls.ava.R

val nunitoLight = FontFamily(Font(R.font.nunito_light))
val nunitoRegular = FontFamily(Font(R.font.nunito_regular))
val nunitoMedium = FontFamily(Font(R.font.nunito_medium))
val nunitoBold = FontFamily(Font(R.font.nunito_bold))
val bakbakone = FontFamily(Font(R.font.bakbakone_regular))
val sequelRegular = FontFamily(Font(R.font.sequel_regular))

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = nunitoRegular,
        fontWeight = FontWeight.Normal,
        fontSize = UiDimensions.text16
    ),
    bodyMedium = TextStyle(
        fontFamily = nunitoLight,
        fontWeight = FontWeight.Light,
        fontSize = UiDimensions.text14
    ),
    titleLarge = TextStyle(
        fontFamily = nunitoBold,
        fontWeight = FontWeight.Bold,
        fontSize = UiDimensions.text20
    ),
    titleMedium = TextStyle(
        fontFamily = bakbakone,
        fontWeight = FontWeight.Medium,
        fontSize = UiDimensions.text18
    ),
    // Puedes agregar más estilos según sea necesario
)
