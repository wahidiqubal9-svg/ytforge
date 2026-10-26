package com.wahidiqubal.ytforge.data.video

data class VideoStyle(
    val id: String,
    val name: String,
    val description: String,
    val bgA: String,
    val bgB: String,
    val accent: String,
    val ink: String,
    val muted: String,
    val card: String,
    val codeBg: String,
    val codeInk: String,
    val light: Boolean = false,
)

object VideoStyles {
    val all = listOf(
        VideoStyle("kinetic", "Kinetic", "Dark kinetic slides", "#07111F", "#14395C", "#3EE6B0", "#F7FBFF", "#8AA0B8", "#0D1D30", "#582E9C", "#FFD700"),
        VideoStyle("neo", "Neo", "Purple social promo", "#1A0B2E", "#582E9C", "#FFD700", "#F7FBFF", "#B7A6D8", "#241246", "#FFD700", "#1A0B2E"),
        VideoStyle("amber", "Amber", "Warm offer card", "#1A0B07", "#5A2A0A", "#FFC857", "#FFF7E8", "#C9A98F", "#2A160B", "#5A2A0A", "#FFC857"),
        VideoStyle("mono", "Mono", "Black and white editorial", "#0A0A0A", "#1A1A1A", "#FFFFFF", "#FAFAFA", "#A3A3A3", "#171717", "#FFFFFF", "#0A0A0A"),
        VideoStyle("editorial", "Editorial", "Cream and red print", "#F4EFE6", "#E8DFD0", "#A91D1D", "#1A1A1A", "#5C554B", "#FFFFFF", "#A91D1D", "#FFD700", light = true),
        VideoStyle("ocean", "Ocean", "Blue tech promo", "#061826", "#0B3B5A", "#7FD7FF", "#F4FBFF", "#93B7C8", "#0D2638", "#0B3B5A", "#7FD7FF"),
        VideoStyle("rose", "Rose", "Warm pink social", "#1A0B14", "#4A1D2A", "#FF8A65", "#FFF4F0", "#C79A90", "#2A111B", "#4A1D2A", "#FFD700"),
        VideoStyle("poster", "Poster", "Yellow bold poster", "#F5C518", "#E0A800", "#07111F", "#07111F", "#4A3C00", "#FFFFFF", "#07111F", "#F5C518", light = true),
    )

    fun byId(id: String): VideoStyle = all.firstOrNull { it.id == id } ?: all.first()
}
