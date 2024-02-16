package datastore.model

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction


class Tables {

    private val connection = Database.connect(
        url = "jdbc:sqlite:data.db",
        driver = "org.sqlite.JDBC"
    )

    object Words : Table() {
        val id = integer("id").autoIncrement().primaryKey()
        val text = varchar("text", 30)
        val translate = varchar("translate", 30)

        override val primaryKey = PrimaryKey(id, name = "PK_Words_id")
    }

    object UserAnswers : Table() {
        val userId = integer("user_id")
        val wordId = integer("word_id").references(Words.id)
        val correctAnswerCount = integer("correct_answer_count")
        val updatedAt = timestamp("updated_at")
    }

    object Users : Table() {
        val id = integer("id").autoIncrement().primaryKey()
        val username = varchar("username", 30)
        val createdAt = timestamp("created_at")
        val chatId = long("chat_id")

        override val primaryKey = PrimaryKey(id, name = "PK_Users_id")
    }

    fun createTables() {
        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Words, UserAnswers, Users)

            val testWord = Words.insert {
                it[id] = 1
                it[text] = "test"
                it[translate] = "тест"
            }
        }
    }

}
