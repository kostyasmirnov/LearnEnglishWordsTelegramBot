import java.io.File
import java.sql.DriverManager
import java.sql.SQLException

const val DEFAULT_LEARNING_THRESHOLD: Int = 3
const val DEFAULT_FILE_NAME: String = "words.txt"

class FileUserDictionary(
    private val fileName: String = DEFAULT_FILE_NAME,
    private val learningThreshold: Int = DEFAULT_LEARNING_THRESHOLD,
) : DatabaseUserDictionary.IUserDictionary {

    private val dictionary = try {
        loadDictionary()
    } catch (e: Exception) {
        throw IllegalArgumentException("Некорректный файл")
    }


    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        dictionary.find { it.original == original }?.correctAnswersCount = correctAnswersCount
        saveDictionary()
        val userId = 1
        val wordId = 0
        try {
            DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate(
                    """
                    UPDATE user_answers
                SET correct_answer_count = correct_answer_count + 1
                WHERE user_id = $userId AND word_id = $wordId;
                """.trimIndent()
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    override fun resetUserProgress(chatId: Long) {
        try {
            DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate(
                    """
                UPDATE user_answers
                SET correct_answer_count = 0
                WHERE user_id = $chatId;
            """.trimIndent()
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun getNumOfLearnedWords(): Int {
        var numOfLearnedWords: Int = 0
        try {
            DriverManager.getConnection("jdbc:sqlite:data.db")
                .use { connection ->
                    val statement = connection.createStatement()
                    val resultSet = statement.executeQuery(
                        """
                    SELECT wa.word_id
                    FROM user_answers wa
                    WHERE wa.correct_answer_count >= $learningThreshold;
                """.trimIndent()
                    )
                    while (resultSet.next()) {
                        numOfLearnedWords++
                    }
                }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return numOfLearnedWords
    }

    override fun getSize(): Int {
        TODO("Not yet implemented")
    }

    override fun getLearnedWords(): List<String> {
        val learnedWords = mutableListOf<String>()

        try {
            DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(
                    """
                SELECT w.text
                FROM words w
                JOIN user_answers ua ON w.id = ua.word_id
                WHERE ua.correct_answer_count >= $learningThreshold;
            """.trimIndent()
                )

                while (resultSet.next()) {
                    val wordText = resultSet.getString("text")
                    learnedWords.add(wordText)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return learnedWords
    }

    override fun getUnlearnedWords(chatId: Long): List<String> {
        val unlearnedWords = mutableListOf<String>()

        try {
            DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(
                    """
                SELECT w.translate
                FROM words w
                LEFT JOIN user_answers ua ON w.id = ua.word_id AND ua.user_id = $chatId
                WHERE ua.correct_answer_count IS NULL OR ua.correct_answer_count <= $learningThreshold;
                """.trimIndent()
                )

                while (resultSet.next()) {
                    val wordText = resultSet.getString("translate")
                    unlearnedWords.add(wordText)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return unlearnedWords
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

    fun updateDictionary(wordsFile: File) {
        try {
            DriverManager.getConnection("jdbc:sqlite:data.db")
                .use { connection ->
                    val statement = connection.createStatement()

                    wordsFile.forEachLine { line ->
                        val (text, translate) = line.split("|")
                        statement.executeUpdate("INSERT INTO words (text, translate) VALUES ('$text', '$translate')")
                    }
                }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun saveDictionary() {
        val file = File(fileName)
        val newFileContent = dictionary.map { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
        file.writeText(newFileContent.joinToString(separator = "\n"))
    }

}