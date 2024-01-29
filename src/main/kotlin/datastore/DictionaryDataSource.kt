package datastore

import java.sql.DriverManager

fun main() {
    DriverManager.getConnection("jdbc:sqlite:data.db")
        .use { connection ->
            val statement = connection.createStatement()
            statement.executeUpdate(
                """
                      CREATE TABLE IF NOT EXISTS "words" (
                          "id" integer PRIMARY KEY,
                          "text" varchar ,
                          "translate" varchar
                      );
              """.trimIndent()
            )
            statement.executeUpdate(
                """
                      CREATE TABLE IF NOT EXISTS "user_answers" (
                        "user_id" integer,
                        "word_id" integer,
                        "correct_answer_count" integer,
                        "updated_at" timestamp
                       );
                    """.trimIndent()
            )
            statement.executeUpdate(
                """
                      CREATE TABLE IF NOT EXISTS "users" (
                        "id" integer PRIMARY KEY,
                         "username" varchar,
                         "created_at" timestamp,
                         "chat_id" integer
                      );
                    """.trimIndent()
            )
        }
}