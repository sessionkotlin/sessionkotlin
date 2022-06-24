package lib

import maxValue
import minValue

/**
 * Returns a long between [minValue] and [maxValue]
 */
fun randomLong() = (minValue..maxValue).random()

fun guess(minV: Long, maxV: Long) = (maxV + minV) / 2
