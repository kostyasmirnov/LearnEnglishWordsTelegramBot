import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.ConnectException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.DriverManager
import java.sql.SQLException

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

class TelegramBotService(
    private val botToken: String,
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val urlSendMessage = "$URL_TG$botToken/sendMessage"
    private val client: HttpClient = HttpClient.newBuilder().build()
    val wordsFile: File = File("words.txt")
    private val fud = FileUserDictionary()


    fun getUpdates(updateId: Long): Response? {
        try {
            val urlUpdate = "$URL_TG$botToken/getUpdates?offset=$updateId"

            val request = HttpRequest.newBuilder().uri(URI.create(urlUpdate)).build()
            val responseText: HttpResponse<String>? =
                runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }.getOrNull()
            val response: Response? = responseText?.body()?.let { json.decodeFromString(it) }
            return response
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
        return null
    }

    fun sendMessage(chatId: Long, message: String): String? {
        try {
            val urlSendMessage = "$URL_TG$botToken/sendMessage"
            val requestBody = SendMessageRequest(
                chatId = chatId,
                text = message,
            )
            val requestBodyString = json.encodeToString(requestBody)

            val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()

            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
        return null
    }

    fun sendMenu(chatId: Long): String? {
        try {
            val requestBody = SendMessageRequest(
                chatId = chatId,
                text = "Основное меню",
                replyMarkup = ReplyMarkup(
                    listOf(
                        listOf(
                            InlineKeyboard(text = "Изучить слова", callbackData = LEARN_WORDS_CLICK),
                            InlineKeyboard(text = "Статистика", callbackData = STATS_CLICK),
                        ),
                        listOf(InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICK))
                    )
                )
            )
            val requestBodyString = json.encodeToString(requestBody)

            val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()

            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
        return null
    }

    private fun sendQuestion(chatId: Long): String? {
        val question: String = fud.getUnlearnedWords(chatId).random().take(1).toString() //должно быть слово на русском
        try {
            val questionVariants = fud.getUnlearnedWords(chatId).take(4).mapIndexed { index, translate ->
                listOf(
                    InlineKeyboard(
                        text = translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                )
            }
            val requestBody = SendMessageRequest(
                chatId = chatId,
                text = question,
                replyMarkup = ReplyMarkup(
                    inlineKeyboard = questionVariants + listOf(
                        listOf(InlineKeyboard(text = "Выйти в основное меню", callbackData = MENU_CLICK))
                    )
                )
            )
            val requestBodyString = json.encodeToString(requestBody)

            val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()

            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
        return null
    }

    fun checkNextQuestionAndSend(chatId: Long) {
        try {
            val question: String = fud.getUnlearnedWords(1263632552).take(1).toString()
            if (question == null) {
                sendMessage(chatId, LEARNED_ALL_WORDS)
            } else {
                sendQuestion(chatId)
            }
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
    }

    fun getAndSaveUserData(chatId: Long) {
        val currentDate = getCurrentDateTime()
        try {
            DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate(
                    """
                INSERT INTO users (chat_id, created_at, username)
                 VALUES ('$chatId', '$currentDate', 'example_user'); 
            """.trimIndent() //придумать как получить и Insert username
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    private fun getCurrentDateTime(): Long {
        return System.currentTimeMillis()
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        TODO()
    }

    fun getStatistics(chatId: Long): String {
        var learned = 0
        var total = 0
        try {
            DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(
                    """
                    SELECT COUNT(*) AS count_of_records
FROM user_answers ua
JOIN users u ON ua.user_id = u.id
WHERE u.chat_id = $chatId
  AND ua.correct_answer_count >= 3
                """.trimIndent()
                )
                if (resultSet.next()) {
                    learned = resultSet.getInt("count_of_records")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        try {
            DriverManager.getConnection("jdbc:sqlite:data.db").use { connection ->
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery(
                    """
                    SELECT COUNT(*) AS count_of_records
                    FROM words
                """.trimIndent()
                )

                if (resultSet.next()) {
                    total = resultSet.getInt("count_of_records")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        val percentLearned: Int = learned * 100 / total
            return "Выучено $learned из $total слов | $percentLearned%"
    }
}

    const val URL_TG = "https://api.telegram.org/bot"
    const val STATS_CLICK = "statistics_click"
    const val LEARN_WORDS_CLICK = "learnWords_click"
    const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
    const val CORRECT = "Правильно!"
    const val NOT_CORRECT = "Неверно"
    const val RESET_CLICK = "reset_click"
    const val MENU_CLICK = "menu_click"