package com.antoniowalls.ava.design

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared dimensions for screens, controls, and typography. */
object UiDimensions {
    val none = 0.dp
    val hairline = 0.5.dp
    val xxs = 1.dp
    val xs = 2.dp
    val tiny = 3.dp
    val small = 4.dp
    val compact = 6.dp
    val base = 8.dp
    val basePlus = 10.dp
    val medium = 12.dp
    val mediumPlus = 14.dp
    val beforeLarge = 15.dp
    val large = 16.dp
    val wide = 20.dp
    val extraLarge = 24.dp
    val extraLargePlus = 25.dp
    val size30 = 30.dp
    val size32 = 32.dp
    val size34 = 34.dp
    val size36 = 36.dp
    val size40 = 40.dp
    val touchTarget = 48.dp
    val size50 = 50.dp
    val size70 = 70.dp
    val size120 = 120.dp
    val size130 = 130.dp
    val size150 = 150.dp
    val size160 = 160.dp
    val size200 = 200.dp
    val size300 = 300.dp

    val text14 = 14.sp
    val text16 = 16.sp
    val text18 = 18.sp
    val text20 = 20.sp
    val text28 = 28.sp
    val text34 = 34.sp
    val text64 = 64.sp

    fun commentSheetHeight(screenHeightDp: Int) = (screenHeightDp * 0.7f).dp.coerceAtLeast(size200)
}
