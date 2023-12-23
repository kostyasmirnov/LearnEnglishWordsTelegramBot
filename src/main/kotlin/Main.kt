import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")
    val dictionary = loadDictionaryFromFile(wordsFile)

    while (true) {
        println(START_MESSAGE)
        val inputForStartMessage: Int = readln().toInt()
        when (inputForStartMessage) {
            1 -> learnWords(dictionary)
            2 -> printStatistic(dictionary)
            0 -> break
            else -> println(WARNING_MESSAGE)
        }
    }

}

fun loadDictionaryFromFile(file: File): MutableList<Word> {
    val dictionary = mutableListOf<Word>()
    val lines: List<String> = file.readLines()
    for (line in lines) {
        val lineParts = line.split("|")
        val word = Word(original = lineParts[0], translate = lineParts[1], correctAnswersCount = lineParts[2].toInt())
        dictionary.add(word)
    }
    return dictionary
}

fun printStatistic(dictionary: List<Word>) {
    val totalWords = dictionary.size
    val learnedWords = dictionary.count { it.correctAnswersCount >= 3 }

    val percentageLearned = if (totalWords > 0) {
        (learnedWords.toDouble() / totalWords.toDouble() * 100).toInt()
    } else 0

    println("Выучено $learnedWords из $totalWords слов | $percentageLearned%")

}

fun learnWords(dictionary: List<Word>) {

    var isLearned = false
    while (!isLearned) {

        val notLearnedWords =
            dictionary.filter { it.correctAnswersCount < 3 }

        if (notLearnedWords.isEmpty()) {
            println(LEARNED_ALL_WORDS)
            isLearned = true
            break
        } else {

            val (mysteryWord, mysteryWordAnswer) = notLearnedWords.first().run { original to translate }
            println("Как переводиться $mysteryWord? Введите номер")
            val variantsAnswer = notLearnedWords.take(4)
                .forEachIndexed { index, word -> println("${index + 1}. ${word.translate}") }
                .also { println("0. Выход в меню") }

            val userAnswer = readln().toInt()
            if (userAnswer in 1..4 && notLearnedWords[userAnswer - 1].translate.equals(
                    mysteryWordAnswer,
                    ignoreCase = true
                )
            ) {
                println("Верно!")
                val wordToUpdate = dictionary.find { it.translate == mysteryWordAnswer }
                wordToUpdate?.let { it.correctAnswersCount++ }
            } else if (userAnswer == 0) break
            else println("Неверно")
        }
    }
}

data class Word(
    val original: String,
    var translate: String,
    var correctAnswersCount: Int = 0,
)

const val START_MESSAGE = "Меню:\n1 - Учить слова\n2 - Статистика\n0 - Выход"
const val WARNING_MESSAGE = "Для выбора пункта из меню, введите цифру"
const val LEARNED_ALL_WORDS = "Вы выучили все слова"