package datastore

import datastore.model.Tables
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import trainer.IUserDictionary
import trainer.model.Word
import java.io.File
import java.sql.Connection
import java.sql.SQLException

class DatabaseUserDictionary(private val connection: Database) : IUserDictionary {

//    private val connection2 = DriverManager.getConnection("jdbc:sqlite:data.db")
    //val connection = Database.connect("jdbc:sqlite:data.db", driver = "org.h2.Driver")

    init {
        updateDictionary(File("words.txt"))
    }

    fun getConnect() {
        val connection = Database.connect("jdbc:sqlite:data.db")
    }

    override fun getNumOfLearnedWords(chatId: Long, learningThreshold: Int): Int {
        var numOfLearnedWords = 0

        try {
            transaction {
                val numOfLearnedWordsResult = Tables.UserAnswers
                    .select {
                        (Tables.UserAnswers.userId eq getUserId(chatId)) and
                                (Tables.UserAnswers.correctAnswerCount greaterEq learningThreshold)
                    }
                    .count()

                numOfLearnedWords = numOfLearnedWordsResult.toInt()
            }
        } catch (e: Exception) {
            println(e)
        }

        return numOfLearnedWords
    }

    override fun getSize(chatId: Long): Int {
        var wordCount = 0

        try {
            transaction {
                val wordCountResult = Tables.Words.selectAll().count()

                wordCount = wordCountResult.toInt()
            }
        } catch (e: Exception) {
            println(e)
        }

        return wordCount
    }

    override fun getLearnedWords(chatId: Long, learningThreshold: Int): List<Word> {
        val learnedWords = mutableListOf<Word>()

        try {
            transaction {
                val resultSet = Tables.UserAnswers
                    .innerJoin(Tables.Words, { wordId }, { id })
                    .select {
                        (Tables.UserAnswers.userId eq getUserId(chatId)) and
                                (Tables.UserAnswers.correctAnswerCount greaterEq learningThreshold)
                    }

                resultSet.forEach {
                    val original = it[Tables.Words.text]
                    val translate = it[Tables.Words.translate]
                    val correctAnswersCount = it[Tables.UserAnswers.correctAnswerCount]

                    learnedWords.add(Word(original, translate, correctAnswersCount))
                }
            }
        } catch (e: Exception) {
            println(e)
        }

        return learnedWords
    }

    override fun getUnlearnedWords(chatId: Long, learningThreshold: Int): List<Word> {
        val unlearnedWords = mutableListOf<Word>()

        try {
            transaction {
                // Подзапросы для получения слов с количеством правильных ответов и без них
                val subQueryWithCorrectAnswers = Tables.UserAnswers
                    .slice(Tables.UserAnswers.wordId, Tables.UserAnswers.correctAnswerCount)
                    .select { Tables.UserAnswers.userId eq getUserId(chatId) }
                    .andWhere { Tables.UserAnswers.correctAnswerCount less learningThreshold }

                val subQueryWithoutCorrectAnswers = Tables.UserAnswers
                    .slice(Tables.UserAnswers.wordId)
                    .select { Tables.UserAnswers.userId eq getUserId(chatId) }
                    .andWhere { Tables.UserAnswers.wordId.isNull() }

                // Выполнение запроса и добавление результатов в список unlearnedWords
                subQueryWithCorrectAnswers.forEach {
                    val original = it[Tables.Words.text]
                    val translate = it[Tables.Words.translate]
                    val correctAnswersCount = it[Tables.UserAnswers.correctAnswerCount]

                    unlearnedWords.add(Word(original, translate, correctAnswersCount))
                }

                subQueryWithoutCorrectAnswers.forEach {
                    val original = it[Tables.Words.text]
                    val translate = it[Tables.Words.translate]
                    val correctAnswersCount = it[Tables.UserAnswers.correctAnswerCount]

                    unlearnedWords.add(Word(original, translate, correctAnswersCount))
                }
            }
        } catch (e: Exception) {
            println(e)
        }
        return unlearnedWords
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int, chatId: Long) {
        var wordId = 0
        val userId = getUserId(chatId)

        try {

            transaction(Connection.TRANSACTION_SERIALIZABLE, 1) {
                // Получение wordId по тексту слова
                val wordIdResult = Tables.Words.slice(Tables.Words.id)
                    .select { Tables.Words.text eq original }
                    .map { it[Tables.Words.id] }
                    .firstOrNull()

                wordId = wordIdResult ?: return@transaction

                // Проверка существования записи в таблице user_answers
                val userAnswerResult = Tables.UserAnswers.select { Tables.UserAnswers.wordId eq wordId }
                    .map { it[Tables.UserAnswers.wordId] }
                    .firstOrNull()

                // Если записи не существует, вставляем новую запись
                if (userAnswerResult == null) {
                    Tables.UserAnswers.insert {
                        it[correctAnswerCount] = correctAnswersCount
                        it[updatedAt] = timestamp("updatedAt")
                        it[Tables.UserAnswers.userId] = userId
                        it[Tables.UserAnswers.wordId] = wordId
                    }
                } else {
                    // Если запись существует, обновляем ее
                    Tables.UserAnswers.update({ (Tables.UserAnswers.userId eq userId) and (Tables.UserAnswers.wordId eq wordId) }) {
                        with(SqlExpressionBuilder) {
                            it.update(correctAnswerCount, correctAnswerCount + correctAnswersCount)
                            it[updatedAt] = timestamp("updatedAt")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    override fun resetUserProgress(chatId: Long) {
        val userId = getUserId(chatId)

        try {
            transaction {
                Tables.UserAnswers.update({ Tables.UserAnswers.userId eq userId }) {
                    it[correctAnswerCount] = 0
                }
            }
        } catch (e: SQLException) {
            println(e)
        }
    }

    private fun getUserId(chatId: Long): Int {
        var userId = 0

        try {
            transaction {
                val userSql = Tables.Users.select { Tables.Users.chatId eq chatId }
                userId = userSql.first().getNullableInt(Tables.Users.chatId)?.toInt() ?: 0
                println(userId)

                if (userId == null) {
                    val insertedId = Tables.Users.insert {
                        it[Tables.Users.chatId] = chatId
                        it[createdAt] = timestamp("createdAt")
                        it[username] = "defaultName"
                    } get Tables.Users.id

                    userId = insertedId
                }
            }
        } catch (e: Exception) {
            println(e)
        }

        return userId
    }

    private fun updateDictionary(wordsFile: File) {
        try {
            transaction {
                wordsFile.forEachLine { line ->
                    val (text, translate) = line.split("|")
                    val wordCount =
                        Tables.Words.select { (Tables.Words.text eq text) and (Tables.Words.translate eq translate) }
                            .count()

                    if (wordCount == 0L) {
                        Tables.Words.insert {
                            it[Tables.Words.text] = text
                            it[Tables.Words.translate] = translate
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun ResultRow.getNullableInt(column: Column<Long>): Long? {
        return try {
            this[column]
        } catch (e: Exception) {
            null
        }
    }
}