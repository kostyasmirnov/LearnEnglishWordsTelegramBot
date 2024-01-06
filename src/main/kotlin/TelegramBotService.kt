import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val botToken: String,
) {

    val json = Json {
        ignoreUnknownKeys = true
    }
    private val urlSendMessage = "$URL_TG$botToken/sendMessage"


    fun getUpdates(updateId: Long): String {
        val urlUpdate = "$URL_TG$botToken/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlUpdate)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    private fun sendMessage(json: Json, chatId: Long, message: String): String {
        val urlSendMessage = "$URL_TG$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendMenu(json: Json, chatId: Long): String {
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

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(json: Json, chatId: Long, question: Question?): String {
        val questionVariants = question?.variants!!.mapIndexed { index, word ->
            listOf(
                InlineKeyboard(
                    text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                )
            )
        }
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                inlineKeyboard = questionVariants + listOf(
                    listOf(InlineKeyboard(text = "Выйти в основное меню", callbackData = MENU_CLICK))
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun checkNextQuestionAndSend(json: Json, trainer: LearnWordsTrainer, chatId: Long) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(json, chatId, LEARNED_ALL_WORDS)
        } else {
            sendQuestion(json, chatId, question)
        }
    }

    fun handleUpdate(update: Update, json: Json, trainers: HashMap<Long, LearnWordsTrainer>) {
        val message = update.message?.text
        val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val data = update.callbackQuery?.data

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

        when {
            message?.lowercase() == "/start" || data == MENU_CLICK -> {
                sendMenu(json, chatId)
            }

            data == LEARN_WORDS_CLICK -> {
                checkNextQuestionAndSend(json, trainer, chatId)
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                val result = trainer.checkAnswer(userAnswerIndex)
                if (result) sendMessage(json, chatId, CORRECT)
                else sendMessage(
                    json,
                    chatId,
                    "$NOT_CORRECT. ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
                )
                checkNextQuestionAndSend(json, trainer, chatId)
            }

            data == STATS_CLICK -> {
                val statistics = trainer.getStatistics()
                val statisticsString =
                    "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percentLearned}%"
                sendMessage(json, chatId, statisticsString)
                Thread.sleep(1000)
                sendMenu(json, chatId)
            }

            data == RESET_CLICK -> {
                trainer.resetProgress()
                sendMessage(json, chatId, "Прогресс сброшен")
                Thread.sleep(1000)
                sendMenu(json, chatId)
            }
        }
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