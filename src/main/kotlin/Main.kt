import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")
    val dictionary = mutableListOf<Word>()


    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toInt())
        dictionary.add(word)
    }

    fun getStatistic() {
        val totalWords = dictionary.size
        val learnedWords = dictionary.count { it.correctAnswersCount >= 3 }

        val percentageLearned = if (totalWords > 0) {
            (learnedWords.toDouble() / totalWords.toDouble() * 100).toInt()
        } else 0

        println("Выучено $learnedWords из $totalWords слов | $percentageLearned%")

    }

    dictionary.forEach { println(it) }

    while (true) {
        println(START_MESSAGE)
        val inputForStartMessage: Int = readln().toInt()
        when (inputForStartMessage) {
            1 -> TODO()
            2 -> getStatistic()
            0 -> break
            else -> println(WARNING_MESSAGE)
        }
    }


}

data class Word(
    val original: String,
    var translate: String,
    val correctAnswersCount: Int = 0,
)

const val START_MESSAGE = "Меню:\n1 - Учить слова\n2 - Статистика\n0 - Выход"
const val WARNING_MESSAGE = "Для выбора пукнта из меню, введите цифру"