package datastore

import trainer.IUserDictionary
import trainer.model.Word
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

class DatabaseUserDictionary : IUserDictionary {

    private val connection = DriverManager.getConnection("jdbc:sqlite:data.db")
    private val statement: Statement = connection.createStatement()

    init {
        updateDictionary(File("words.txt"))
    }

    override fun getNumOfLearnedWords(chatId: Long, learningThreshold: Int): Int {
        var numOfLearnedWords = 0

        try {
            val resultSet = statement.executeQuery(
                """
                SELECT COUNT(*) FROM user_answers 
                WHERE user_id = ${getUserId(chatId)}
                AND correct_answer_count >= $learningThreshold
            """.trimIndent()
            )

            if (resultSet.next()) numOfLearnedWords = resultSet.getInt("COUNT(*)")
        } catch (e: SQLException) {
            println(e)
        }

        return numOfLearnedWords
    }

    override fun getSize(chatId: Long): Int {
        var wordCount = 0

        try {
            val resultSet = statement.executeQuery(
                """
                SELECT COUNT(*) as totalWords FROM words
            """.trimIndent()
            )

            if (resultSet.next()) wordCount = resultSet.getInt("totalWords")
        } catch (e: SQLException) {
            println(e)
        }

        return wordCount
    }

    override fun getLearnedWords(chatId: Long, learningThreshold: Int): List<Word> {
        val learnedWords = mutableListOf<Word>()

        try {
            val resultSet = statement.executeQuery(
                """
                SELECT words.text, words.translate, user_answers.correct_answer_count
                FROM user_answers
                INNER JOIN words ON user_answers.word_id = words.id
                WHERE user_answers.user_id = ${getUserId(chatId)}
                AND user_answers.correct_answer_count >= $learningThreshold
            """.trimIndent()
            )


            while (resultSet.next()) {
                val original = resultSet.getString("text")
                val translate = resultSet.getString("translate")
                val correctAnswersCount = resultSet.getInt("correct_answer_count")

                learnedWords.add(Word(original, translate, correctAnswersCount))
            }

        } catch (e: SQLException) {
            println(e)
        }

        return learnedWords
    }

    override fun getUnlearnedWords(chatId: Long, learningThreshold: Int): List<Word> {
        val unlearnedWords = mutableListOf<Word>()

        try {
            val wordsSQLResult = statement.executeQuery(
                """
                SELECT words.text, words.translate, user_answers.correct_answer_count
                FROM user_answers
                INNER JOIN words ON user_answers.word_id = words.id
                WHERE user_answers.user_id = ${getUserId(chatId)}
                AND user_answers.correct_answer_count < $learningThreshold
            """.trimIndent()
            )

            while (wordsSQLResult.next()) {
                val original = wordsSQLResult.getString("text")
                val translate = wordsSQLResult.getString("translate")
                val correctAnswersCount = wordsSQLResult.getInt("correct_answer_count")

                unlearnedWords.add(Word(original, translate, correctAnswersCount))
            }

            if (unlearnedWords.size == 0) {
                val allWordsIsUnlearned = statement.executeQuery(
                    """
                    SELECT text, translate FROM words
                """.trimIndent()
                )
                while (allWordsIsUnlearned.next()) {
                    val original = allWordsIsUnlearned.getString("text")
                    val translate = allWordsIsUnlearned.getString("translate")
                    val correctAnswersCount = 0

                    unlearnedWords.add(Word(original, translate, correctAnswersCount))
                }
            }

        } catch (e: SQLException) {
            println(e)
        }
        return unlearnedWords
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int, chatId: Long) {
        var wordId = 0
        val userId = getUserId(chatId)

        try {
            val wordIdResult = statement.executeQuery(
                """
                SELECT id FROM words
                WHERE text = '$original'
            """.trimIndent()
            )
            if (wordIdResult.next()) wordId = wordIdResult.getInt("id")

            statement.executeUpdate(
                """
                UPDATE user_answers
                SET correct_answer_count = correct_answer_count + $correctAnswersCount, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = $userId AND word_id = $wordId;
            """.trimIndent()
            )

        } catch (e: SQLException) {
            println(e)
        }
    }

    override fun resetUserProgress(chatId: Long) {
        val userId = getUserId(chatId)

        try {
            statement.executeUpdate(
                """
                UPDATE user_answers
                SET correct_answer_count = 0
                WHERE user_id = $userId;
            """.trimIndent()
            )
        } catch (e: SQLException) {
            println(e)
        } finally {
            statement.close()
            connection.close()
        }
    }

    private fun getUserId(chatId: Long): Int {
        var userId = 0

        try {
            statement.executeUpdate("""
                INSERT INTO users (chat_id, created_at, username)
                VALUES ($chatId, CURRENT_TIMESTAMP, 'defaultName')
            """.trimIndent()
            )

            statement.generatedKeys.use { generatedKeys -> //получение последнего сгенерированного id
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1)
                } else {
                    throw SQLException("Creating user failed, no ID obtained.")
                    println("nice")
                }
            }
        } catch (e: SQLException) {
            println(e)
        }

        return userId
    }

    fun updateDictionary(wordsFile: File) {
        try {
            wordsFile.forEachLine { line ->
                val (text, translate) = line.split("|")

                val resultSet =
                    statement.executeQuery("SELECT COUNT(*) FROM words WHERE text = '$text' AND translate = '$translate'")
                resultSet.next()
                val wordCount = resultSet.getInt(1)

                if (wordCount == 0) {
                    statement.executeUpdate("INSERT INTO words (text, translate) VALUES ('$text', '$translate')")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

//    fun insertNewUser(chatId: Long) {
//        try {
//            val resultSet = statement.executeQuery("SELECT COUNT(*) FROM users WHERE chat_id = $chatId")
//            resultSet.next()
//            val userCount = resultSet.getInt(1)
//            if (userCount == 0) {
//                statement.executeUpdate(
//                    """
//                    INSERT INTO users (chat_id, created_at, username)
//                    VALUES ($chatId, CURRENT_TIMESTAMP, 'defaultName')
//                """.trimIndent()
//                )
//            }
//
//        } catch (e: SQLException) {
//            println(e)
//        }
//    }

//    fun insertNewUserAnswers(chatId: Long) {
//        val userId = getUserId(chatId)
//        val allWordsCount: Int
//        var wordsUser: Int
//        try {
//            val allWordsCountResult = statement.executeQuery(
//                """
//            SELECT COUNT(id) FROM words
//            """.trimIndent()
//            )
//            allWordsCount = allWordsCountResult.getInt("COUNT(id)")
//
//            val wordsUserResult = statement.executeQuery(
//                """
//                SELECT COUNT(word_id)
//                FROM user_answers WHERE user_id = $userId
//            """.trimIndent()
//            )
//            wordsUser = wordsUserResult.getInt("COUNT(word_id)")
//
//            if (allWordsCount != wordsUser) {
//                val countNewWordsForUser = allWordsCount - wordsUser
//                var wordId = allWordsCount - countNewWordsForUser
//                while (wordsUser != allWordsCount) {
//                    statement.executeUpdate(
//                        """
//                INSERT INTO user_answers (correct_answer_count, updated_at, user_id, word_id)
//                VALUES (0, CURRENT_TIMESTAMP, $userId, $wordId)
//                """.trimIndent()
//                    )
//                    Thread.sleep(200)
//                    ++wordId
//                    ++wordsUser
//                }
//            }
//        } catch (e: SQLException) {
//            println(e)
//        }
//    }
}