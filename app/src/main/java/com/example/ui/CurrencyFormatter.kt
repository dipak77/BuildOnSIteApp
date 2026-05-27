package com.example.ui

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

fun formatIndianRupees(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val standardFormatted = formatter.format(amount)
    val withRupeeSymbol = if (standardFormatted.endsWith(".00")) {
        standardFormatted.substring(0, standardFormatted.length - 3)
    } else {
        standardFormatted
    }
    return withRupeeSymbol
}

fun formatIndianRupeesWithLakhCr(amount: Double): String {
    val standard = formatIndianRupees(amount)
    val absAmt = abs(amount)
    
    val lakhCrSuffix = when {
        absAmt >= 10_000_000.0 -> {
            val cr = amount / 10_000_000.0
            String.format(Locale.US, " (%.2f Cr)", cr)
        }
        absAmt >= 100_000.0 -> {
            val lakh = amount / 100_000.0
            String.format(Locale.US, " (%.2f Lakh)", lakh)
        }
        else -> ""
    }
    return "$standard$lakhCrSuffix"
}
