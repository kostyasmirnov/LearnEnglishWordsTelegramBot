package bot

import datastore.DatabaseUserDictionary
import datastore.FileUserDictionary
import org.jetbrains.exposed.sql.Database
import trainer.IUserDictionary
import trainer.LearnWordsTrainer
import java.net.ConnectException

const val BOT_POLLING_DELAY: Long = 2000
fun main(args: Array<String>) {

    val botToken: String = args[0]
    val telegramBotService = TelegramBotService(botToken = botToken)
    var lastUpdateId: Long? = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()
    val databaseConnection: IUserDictionary = DatabaseUserDictionary(Database.connect("jdbc:sqlite:data.db"))
        val wordsFile: IUserDictionary = FileUserDictionary()


    while (true) {
        Thread.sleep(BOT_POLLING_DELAY)
        val response: Response? = lastUpdateId?.let { telegramBotService.getUpdates(it) }
        if (response?.result?.isEmpty() == true) continue
        println(response)

        val sortedUpdates = response?.result?.sortedBy { it.updateId }
        sortedUpdates?.forEach { update ->
            handleUpdate(
                telegramBotService = telegramBotService,
                update = update,
                trainers = trainers,
                userDictionary = DatabaseUserDictionary(Database.connect("jdbc:sqlite:data.db"))
            )
        }
        lastUpdateId = sortedUpdates?.last()?.updateId?.plus(1)
    }
}

fun handleUpdate(
    telegramBotService: TelegramBotService,
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
    userDictionary: IUserDictionary,
) {
    try {
        val message = update.message?.text
        val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val data = update.callbackQuery?.data

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer(chatId, userDictionary) }

        when {
            message?.lowercase() == "/start" || data == MENU_CLICK -> {
                telegramBotService.sendMenu(chatId)
            }

            data == LEARN_WORDS_CLICK -> {
                telegramBotService.checkNextQuestionAndSend(trainer, chatId)
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                val result = trainer.checkAnswer(userAnswerIndex)
                if (result) telegramBotService.sendMessage(chatId, CORRECT)
                else telegramBotService.sendMessage(
                    chatId,
                    "$NOT_CORRECT. ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
                )
                telegramBotService.checkNextQuestionAndSend(trainer, chatId)
            }

            data == STATS_CLICK -> {
                val statistics = trainer.getStatistics()
                val statisticsString =
                    "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percentLearned}%"
                telegramBotService.sendMessage(chatId, statisticsString)
                Thread.sleep(1000)
                telegramBotService.sendMenu(chatId)
            }

            data == RESET_CLICK -> {
                trainer.resetProgress()
                telegramBotService.sendMessage(chatId, "Прогресс сброшен")
                Thread.sleep(1000)
                telegramBotService.sendMenu(chatId)
            }
        }
    } catch (e: ConnectException) {
        e.printStackTrace()
    }
}