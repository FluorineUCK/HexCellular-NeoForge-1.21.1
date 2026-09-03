package miyucomics.hexcellular

import kotlin.random.Random

private val consonants = charArrayOf('j', 'k', 'l', 'm', 'n', 'p', 's', 't', 'w')
private val vowels = charArrayOf('a', 'e', 'i', 'o', 'u')
private val bannedSyllables = setOf("ji", "ti", "wo", "wu")
private val weights = listOf(2, 5, 3)
private val scannedWeights = weights.runningFold(0) { sum, weight -> sum + weight }.drop(1)
private val weightPeak = scannedWeights.last()

fun generatePropertyName(random: Random = Random.Default): String {
    val word = StringBuilder()
    if (random.nextBoolean()) {
        word.append(vowels.random(random))
        if (random.nextBoolean()) {
            word.append('n')
        }
    }

    repeat(generateNumberOfSyllables(random)) {
        word.append(generateSyllable(word.endsWith('n'), random))
    }
    return word.toString()
}

private fun generateNumberOfSyllables(random: Random): Int {
    val index = random.nextInt(1, weightPeak + 1)
    return scannedWeights.indexOfFirst { index <= it } + 1
}

private fun generateSyllable(wasNasal: Boolean, random: Random): String {
    var consonant = consonants.random(random)
    var vowel = vowels.random(random)
    var syllable = "$consonant$vowel"
    while ((wasNasal && (consonant == 'm' || consonant == 'n')) || syllable in bannedSyllables) {
        consonant = consonants.random(random)
        vowel = vowels.random(random)
        syllable = "$consonant$vowel"
    }
    if (random.nextBoolean() && !syllable.startsWith('n')) {
        syllable += 'n'
    }
    return syllable
}
