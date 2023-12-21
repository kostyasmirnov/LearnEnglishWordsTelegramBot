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

    fun printStatistic(): String {
        val totalWords = dictionary.size
        val learnedWords = dictionary.count { it.correctAnswersCount >= 3 }

        val percentageLearned = if (totalWords > 0) {
            (learnedWords.toDouble() / totalWords.toDouble() * 100).toInt()
        } else 0

        return "Выучено $learnedWords из $totalWords слов | $percentageLearned%"

    }

    fun learnWords() {
        while (true) {

            val notLearnedWords =
                dictionary.filter { it.correctAnswersCount < 3 }
                    .mapIndexed { index: Int, word: Word -> word.translate }

            if (notLearnedWords.isEmpty()) {
                println(LEARNED_ALL_WORDS)
                break
            } else {

                val variantsAnswer: String = dictionary.filter { it.correctAnswersCount < 3 }
                    .mapIndexed { index: Int, word: Word -> "${index + 1}. ${word.translate}" }.toString()

                val mysteryWord: String = dictionary.filter { it.correctAnswersCount < 3 }
                    .mapIndexed { index: Int, word: Word -> word.original }.take(1).toString()
                val mysteryWordAnswer: String = dictionary.filter { it.correctAnswersCount < 3 }
                    .mapIndexed { index: Int, word: Word -> word.translate }.take(1).firstOrNull().toString()

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

    dictionary.forEach { println(it) }

    while (true) {
        println(START_MESSAGE)
        val inputForStartMessage: Int = readln().toInt()
        when (inputForStartMessage) {
            1 -> learnWords()
            2 -> printStatistic()
            0 -> break
            else -> println(WARNING_MESSAGE)
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