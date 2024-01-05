import java.io.File

data class Statistics(
    val learned: Int,
    val total: Int,
    val percentLearned: Int,
)

data class Word(
    val original: String,
    var translate: String,
    var correctAnswersCount: Int = 0,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
) {

    var question: Question? = null
    val wordsFile: File = File("words.txt")
    val dictionary = loadDictionaryFromFile(wordsFile)

    fun getStatistics(): Statistics {
        val learned = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.size
        val total = dictionary.size
        val percentLearned = learned * 100 / total
        return Statistics(learned, total, percentLearned)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            notLearnedList.shuffled()
                .take(countOfQuestionWords) + learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()
        val correctAnswer = questionWords.random()
        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionaryFromFile(file: File): MutableList<Word> {
        try {
            val dictionary = mutableListOf<Word>()
            val lines: List<String> = file.readLines()
            for (line in lines) {
                val lineParts = line.split("|")
                val word =
                    Word(original = lineParts[0], translate = lineParts[1], correctAnswersCount = lineParts[2].toInt())
                dictionary.add(word)
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    private fun saveDictionary(dictionary: List<Word>) {
        wordsFile.writeText("")
        dictionary.forEach {
            val word =
                Word(original = it.original, translate = it.translate, correctAnswersCount = it.correctAnswersCount)
            wordsFile.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

}