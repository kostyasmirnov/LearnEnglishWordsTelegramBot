const val stage8_text = "Hello"

fun main(args: Array<String>) {

    val botToken: String = args[0]

    val tbs = TelegramBotService(botToken = botToken)
    val trainer = LearnWordsTrainer()

    var updateId: Int = 0
    var text: String = ""
    var chatId: Int = 0

    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val messageChatIdRegex: Regex = "\"chat\":\\{\"id\":(.+?),".toRegex()
    val dataRegex: Regex = "\"data\":\"(.*?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        tbs.updates = tbs.getUpdates(updateId)
        println(tbs.updates)

        val matchResultUpdateId = updateIdRegex.find(tbs.updates) ?: continue
        val matchResultText = messageTextRegex.find(tbs.updates)?.groups?.get(1)?.value
        val matchResultChatId = messageChatIdRegex.find(tbs.updates) ?: continue
        val data = dataRegex.find(tbs.updates)?.groups?.get(1)?.value

        updateId = matchResultUpdateId.groupValues[1].toInt() + 1

        chatId = matchResultChatId.groupValues[1].toInt()

        text = matchResultText.toString()

        if (data == LEARN_WORDS_CLICK && chatId != null) {
            tbs.checkNextQuestionAndSend(trainer, chatId)
        }

        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            val result = trainer.checkAnswer(userAnswerIndex)
            if (result) tbs.sendMessage(chatId, CORRECT)
            else tbs.sendMessage(
                chatId,
                "$NOT_CORRECT. ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
            )
        }
        tbs.checkNextQuestionAndSend(trainer, chatId)

        if (text.lowercase() == "/start" && chatId != null) {
            tbs.sendMenu(chatId)
        }

        if (data?.lowercase() == STATS_CLICK && chatId != null) {
            val statistics = trainer.getStatistics()
            val statisticsString =
                "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percentLearned}%"
            tbs.sendMessage(chatId, statisticsString)
        }


    }
}