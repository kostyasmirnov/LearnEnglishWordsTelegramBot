import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    var updates: String = "",
    private val botToken: String,
) {
    fun getUpdates(updateId: Int): String {
        val urlUpdate = "$URL_TG$botToken/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlUpdate)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Int, message: String): String {
        message == stage8_text
        val urlSendMessage = "$URL_TG$botToken/sendMessage?chat_id=$chatId&text=$message"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}

const val URL_TG = "https://api.telegram.org/bot"