package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default Canonical HomeHelp Marketplace Palette (Tailwind Modern Indigo)
var BrandBluePrimary = Color(0xFF4F46E5) // Clean Signature Indigo
var BrandBlueDark = Color(0xFF3730A3)
var BrandBlueLight = Color(0xFF818CF8)

var BrandAmberAccent = Color(0xFFF59E0B) // Amber Gold Accent
var BrandAmberLight = Color(0xFFFCD34D)
var BrandAmberDark = Color(0xFFD97706)

// Operational & Status Colors
val StatusOnlineGreen = Color(0xFF10B981) // Tailwind Emerald-500
val StatusOfflineGrey = Color(0xFF64748B)
val StatusPendingYellow = Color(0xFFF59E0B)
val StatusErrorRed = Color(0xFFEF4444)
val StatusVerifiedBlue = Color(0xFF0284C7)

// Surface & Neutral Colors
val SurfaceLight = Color(0xFFF8FAFC)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceCardSubtle = Color(0xFFF1F5F9)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF475569)
val TextMuted = Color(0xFF94A3B8)
val BorderLight = Color(0xFFE2E8F0)

// Dark Theme Surfaces
val SurfaceDark = Color(0xFF0F172A)
val SurfaceCardDark = Color(0xFF1E293B)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFFCBD5E1)

enum class BrandThemeOption(
    val title: String,
    val hexCode: String,
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val accent: Color
) {
    INDIGO(
        title = "Signature Indigo (Tailwind Default)",
        hexCode = "#4F46E5",
        primary = Color(0xFF4F46E5),
        primaryDark = Color(0xFF3730A3),
        primaryLight = Color(0xFF818CF8),
        accent = Color(0xFFF59E0B)
    ),
    VIOLET(
        title = "Urban Violet / Purple",
        hexCode = "#7C3AED",
        primary = Color(0xFF7C3AED),
        primaryDark = Color(0xFF5B21B6),
        primaryLight = Color(0xFFA78BFA),
        accent = Color(0xFF10B981)
    ),
    ROYAL_BLUE(
        title = "Modern Royal Blue",
        hexCode = "#2563EB",
        primary = Color(0xFF2563EB),
        primaryDark = Color(0xFF1D4ED8),
        primaryLight = Color(0xFF60A5FA),
        accent = Color(0xFFF97316)
    ),
    EMERALD(
        title = "Fresh Emerald Green",
        hexCode = "#059669",
        primary = Color(0xFF059669),
        primaryDark = Color(0xFF065F46),
        primaryLight = Color(0xFF34D399),
        accent = Color(0xFFF59E0B)
    ),
    DARK_SLATE(
        title = "Midnight Minimalist Slate",
        hexCode = "#0F172A",
        primary = Color(0xFF0F172A),
        primaryDark = Color(0xFF020617),
        primaryLight = Color(0xFF334155),
        accent = Color(0xFF38BDF8)
    )
}
