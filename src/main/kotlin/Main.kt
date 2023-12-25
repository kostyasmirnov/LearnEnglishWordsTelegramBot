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
    while (true) {
        val notLearnedWords = dictionary.filter { it.correctAnswersCount < FILTER_CORRECT_ANSWERS }
        val variantsAnswer: List<Word> = notLearnedWords.shuffled().take(NUMBER_OF_ANSWER_OPTIONS)

        if (notLearnedWords.isEmpty()) {
            println(LEARNED_ALL_WORDS)
            break
        }

        val mysteryWord: Word = variantsAnswer.random()

        println("Как переводиться ${mysteryWord.original}? Введите номер")
        variantsAnswer.forEachIndexed { index, word -> println("${index + 1}. ${word.translate}") }
        println("0. Выход в меню")

        val indexOfMysteryWord: Int =
            variantsAnswer.indexOfFirst { it.original.equals(mysteryWord.original, ignoreCase = true) }

        val userAnswer = readln().toInt()
        if (userAnswer in 1..4 && indexOfMysteryWord != -1 && userAnswer - 1 == indexOfMysteryWord) {
            println("Верно!")
            val wordToUpdate = dictionary.find { it.translate == mysteryWord.translate }
            wordToUpdate?.let { it.correctAnswersCount++ }
        } else if (userAnswer == 0) break
        else println("Неверно")
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
const val NUMBER_OF_ANSWER_OPTIONS = 4
const val FILTER_CORRECT_ANSWERS = 3