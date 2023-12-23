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

            val variantsAnswer: String =
                notLearnedWords.mapIndexed { index: Int, word: Word -> word.translate }.shuffled().toString()
            val (mysteryWord, mysteryWordAnswer) = notLearnedWords.first().run { original to translate }
            println("Как переводиться $mysteryWord?\n$variantsAnswer")

            val userAnswer = readln()
            if (userAnswer.equals(mysteryWordAnswer, ignoreCase = true)) {
                println("Верно!")
                val wordToUpdate = dictionary.find { it.translate == userAnswer }
                wordToUpdate?.let { it.correctAnswersCount++ }
            } else println("Неверно")
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