const val stage8_text = "Hello"

fun main(args: Array<String>) {

    val botToken: String = args[0]

    val tbs = TelegramBotService(botToken=botToken)
    var updateId: Int = 0
    var text: String = ""
    var chatId: Int = 0

    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val messageChatIdRegex: Regex = "\"chat\":\\{\"id\":(.+?),".toRegex()

    while (true) {
        Thread.sleep(2000)
        tbs.updates = tbs.getUpdates(updateId)
        println(tbs.updates)

        val matchResultUpdateId = updateIdRegex.find(tbs.updates) ?: continue
        val matchResultText = messageTextRegex.find(tbs.updates)
        val matchResultChatId = messageChatIdRegex.find(tbs.updates) ?: continue

        if (matchResultUpdateId != null) {
            updateId = matchResultUpdateId.groupValues[1].toInt() + 1
            println(updateId)
        }
        if (matchResultChatId != null) {
            chatId = matchResultChatId.groupValues[1].toInt()
            println(chatId)
        }

        if (matchResultText != null && matchResultText.groupValues[1] == stage8_text) {
            println(chatId)
            tbs.sendMessage(chatId, stage8_text)
        }

    }
}



