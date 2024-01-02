import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    var updates: String = "",
) {
    fun getUpdates(botToken: String, updateId: Int): String {
        val urlUpdate = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlUpdate)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(botToken: String, chatId: Int, message: String): String {
        message == stage8_text
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$message"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}