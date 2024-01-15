import java.net.ConnectException

fun main(args: Array<String>) {

    val botToken: String = args[0]
    val tbs = TelegramBotService(botToken = botToken)
    var lastUpdateId: Long? = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()
   // tbs.updateDictionary(tbs.wordsFile)
    val fud = FileUserDictionary()
    //fud.updateDictionary(tbs.wordsFile)


    fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>) {
        try {
            val message = update.message?.text
            val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
            val data = update.callbackQuery?.data

            val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

            when {
                message?.lowercase() == "/start" || data == MENU_CLICK -> { // done
                    tbs.sendMenu(chatId)
                    tbs.getAndSaveUserData(chatId)
                }

                data == LEARN_WORDS_CLICK -> { // done
                    tbs.checkNextQuestionAndSend(chatId)
                }

                data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                    val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                    val result = trainer.checkAnswer(userAnswerIndex) // refactoring
                    if (result) tbs.sendMessage(chatId, CORRECT)
                    else tbs.sendMessage(
                        chatId,
                        "$NOT_CORRECT. ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}" // need refactoring
                    )
                    tbs.checkNextQuestionAndSend(chatId) // done
                }

                data == STATS_CLICK -> {
                    val statistics = tbs.getStatistics(chatId) // done
                    tbs.sendMessage(chatId, statistics)
                    Thread.sleep(1000)
                    tbs.sendMenu(chatId)
                }

                data == RESET_CLICK -> { //done
                    fud.resetUserProgress(chatId)
                    tbs.sendMessage(chatId, "Прогресс сброшен")
                    Thread.sleep(1000)
                    tbs.sendMenu(chatId)
                }
            }
        } catch (e: ConnectException) {
            e.printStackTrace()
        }
    }

    while (true) {
        Thread.sleep(2000)
        val response: Response? = lastUpdateId?.let { tbs.getUpdates(it) }
        if (response?.result?.isEmpty() == true) continue
        println(response)

        val sortedUpdates = response?.result?.sortedBy { it.updateId }
        sortedUpdates?.forEach { handleUpdate(it, trainers) }
        lastUpdateId = sortedUpdates?.last()?.updateId?.plus(1)
    }
}