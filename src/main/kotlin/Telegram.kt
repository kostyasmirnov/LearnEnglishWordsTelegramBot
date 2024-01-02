const val stage8_text = "Hello"

fun main(args: Array<String>) {

    val botToken: String = args[0]

    val tbs = TelegramBotService()
    var updateId: Int = 0
    var text: String = ""
    var chatId: Int = 0

    while (true) {
        Thread.sleep(2000)
        tbs.updates = tbs.getUpdates(botToken, updateId)
        println(tbs.updates)

        val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val messageChatIdRegex: Regex = "\"chat\":\\{\"id\":(.+?),".toRegex()

        val matchResultUpdateId = updateIdRegex.find(tbs.updates)
        val matchResultText = messageTextRegex.find(tbs.updates)
        val matchResultChatId = messageChatIdRegex.find(tbs.updates)

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
            tbs.sendMessage(botToken, chatId, stage8_text)
        }

    }
}



