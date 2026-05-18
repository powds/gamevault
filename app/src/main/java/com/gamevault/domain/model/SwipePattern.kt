package com.gamevault.domain.model

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class SwipePattern(
    val points: List<Pair<Float, Float>> = emptyList()
) {
    companion object {
        // Secret pattern: draw a "Z" shape on the grid
        val SECRET_PATTERN = listOf(
            Pair(0f, 0f),   // top-left
            Pair(3f, 0f),   // top-right
            Pair(0f, 3f),   // bottom-left
            Pair(3f, 3f)    // bottom-right
        )
    }
}