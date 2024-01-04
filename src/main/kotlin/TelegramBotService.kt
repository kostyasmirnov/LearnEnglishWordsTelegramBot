import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(
    var updates: String = "",
    private val botToken: String,
) {

    private val trainer = try {
        LearnWordsTrainer(3, 4)

    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
    }
    private val urlSendMessage = "$URL_TG$botToken/sendMessage"


    fun getUpdates(updateId: Int): String {
        val urlUpdate = "$URL_TG$botToken/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlUpdate)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Int, message: String): String {
        val encodedMessage = URLEncoder.encode(
            message,
            StandardCharsets.UTF_8
        )
        val urlSendMessage = "$URL_TG$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMenu(chatId: Int): String {
        val sendMenuBody = """
            {
                "chat_id": "$chatId",
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "learnWords_click"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "statistics_click"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(chatId: Int, question: Question?): String {
        val allWords = question?.variants
        val sendNextQuestion = """
            {
                "chat_id": $chatId,
                "text": "${question?.correctAnswer?.original}",
                "callback_data": "question",
                "reply_markup": {
                "inline_keyboard": [
                            ${
            allWords?.mapIndexed { index, answer ->
                "[{\"text\": \"${answer.translate}\", \"callback_data\": \"${CALLBACK_DATA_ANSWER_PREFIX + index}\"}]"
            }?.joinToString(",")
        }
        
        ,[
                {
                "text": "Выйти",
            	"callback_data": "exit_btn"
                }
                            ]
                        ]
                    }
                }
        """.trimIndent()

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendNextQuestion))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response)
        println(request)
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Int) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, LEARNED_ALL_WORDS)
        } else {
            sendQuestion(chatId, question)
        }
    }


}

const val URL_TG = "https://api.telegram.org/bot"
const val STATS_CLICK = "statistics_click"
const val LEARN_WORDS_CLICK = "learnWords_click"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val CORRECT = "Правильно!"
const val NOT_CORRECT = "Неверно"