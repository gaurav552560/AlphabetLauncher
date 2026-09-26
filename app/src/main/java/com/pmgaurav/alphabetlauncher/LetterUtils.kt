package com.pmgaurav.alphabetlauncher

fun letterFromName(name: String): Char {
    val firstCharacter = name
        .trim()
        .firstOrNull()

    return if (firstCharacter?.isLetter() == true) {
        firstCharacter.uppercaseChar()
    } else {
        '#'
    }
}

fun groupNamesByLetter(
    appNames: List<String>
): Map<Char, List<String>> {
    return appNames.groupBy { name ->
        letterFromName(name)
    }
}
fun letterFromTouchY(
    y: Float,
    alphabetHeight: Int
): Char? {

    if (alphabetHeight <= 0) {
        return null
    }

    val clampedY = y.coerceIn(
        0f,
        alphabetHeight.toFloat()
    )

    val index = (clampedY / alphabetHeight * 29)
        .toInt()
        .coerceIn(0, 28)

    return when (index) {
        0 -> '★'
        28 -> '•'
        else -> {
            val letterIndex = index - 1

            if (letterIndex == 26) {
                '#'
            } else {
                ('A'.code + letterIndex).toChar()
            }
        }
    }
}