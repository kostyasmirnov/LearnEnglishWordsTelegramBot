
fun main(args: Array<String>) {

    val botToken: String = args[0]
    val tbs = TelegramBotService(botToken = botToken)
    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>) {
        val message = update.message?.text
        val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val data = update.callbackQuery?.data

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

        when {
            message?.lowercase() == "/start" || data == MENU_CLICK -> {
                tbs.sendMenu(chatId)
            }

            data == LEARN_WORDS_CLICK -> {
                tbs.checkNextQuestionAndSend(trainer, chatId)
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                val result = trainer.checkAnswer(userAnswerIndex)
                if (result) tbs.sendMessage(chatId, CORRECT)
                else tbs.sendMessage(
                    chatId,
                    "$NOT_CORRECT. ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
                )
                tbs.checkNextQuestionAndSend(trainer, chatId)
            }

            data == STATS_CLICK -> {
                val statistics = trainer.getStatistics()
                val statisticsString =
                    "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percentLearned}%"
                tbs.sendMessage(chatId, statisticsString)
                Thread.sleep(1000)
                tbs.sendMenu(chatId)
            }

            data == RESET_CLICK -> {
                trainer.resetProgress()
                tbs.sendMessage(chatId, "Прогресс сброшен")
                Thread.sleep(1000)
                tbs.sendMenu(chatId)
            }
        }
    }

    while (true) {
        Thread.sleep(2000)
        val responseString: String = tbs.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = tbs.json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}