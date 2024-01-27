import java.io.File

const val DEFAULT_LEARNING_THRESHOLD: Int = 3
const val DEFAULT_FILE_NAME: String = "words.txt"

class FileUserDictionary(
    private val fileName: String = DEFAULT_FILE_NAME,
) : IUserDictionary {

    private val dictionary = try {
        loadDictionary()
    } catch (e: Exception) {
        throw IllegalArgumentException("Некорректный файл")
    }

    override fun getNumOfLearnedWords(chatId: Long): Int {
        val learnedWordsCount = dictionary.filter { it.correctAnswersCount >= DEFAULT_LEARNING_THRESHOLD }.size
        return learnedWordsCount
    }

    override fun getSize(chatId: Long): Int {
        val allWordsCount = dictionary.count()
        return allWordsCount
    }

    override fun getLearnedWords(chatId: Long, learningThreshold: Int): List<Word> {
        val learnedWords = dictionary.filter { it.correctAnswersCount >= DEFAULT_LEARNING_THRESHOLD }
        return learnedWords
    }

    override fun getUnlearnedWords(chatId: Long, learningThreshold: Int): List<Word> {
        val unlearnedWords = dictionary.filter { it.correctAnswersCount <= DEFAULT_LEARNING_THRESHOLD }
        return unlearnedWords
    }


    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int, chatId: Long) {
        dictionary.find { it.original == original }?.correctAnswersCount = correctAnswersCount
        saveDictionary()
    }

    override fun resetUserProgress(chatId: Long) {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()
            wordsFile.readLines().forEach {
                val splitLine = it.split("|")
                dictionary.add(Word(splitLine[0], splitLine[1], splitLine[2].toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    private fun saveDictionary() {
        val file = File(fileName)
        val newFileContent = dictionary.map { "${it.original}|${it.translate}|${it.correctAnswersCount+1}" }
        file.writeText(newFileContent.joinToString(separator = "\n"))
    }
}