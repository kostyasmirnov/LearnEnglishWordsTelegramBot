import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

//@Serializable
//data class Update(
//    @SerialName("update_id")
//    val updateId: Long,
//    @SerialName("message")
//    val message: Message? = null,
//    @SerialName("callback_query")
//    val callbackQuery: CallbackQuery? = null,
//)
//
//@Serializable
//data class Response(
//    @SerialName("result")
//    val result: List<Update>,
//)
//
//@Serializable
//data class Message(
//    @SerialName("text")
//    val text: String,
//)
//
//@Serializable
//data class CallbackQuery(
//    @SerialName("date")
//    val date: String,
//)

fun main() {



    val responseString = """
        {
        	"ok": true,
        	"result": [
        		{
        			"update_id": 64916719,
        			"message": {
        				"message_id": 266,
        				"from": {
        					"id": 1263632552,
        					"is_bot": false,
        					"first_name": "Kostya",
        					"username": "kostyazicus",
        					"language_code": "ru",
        					"is_premium": true
        				},
        				"chat": {
        					"id": 1263632552,
        					"first_name": "Kostya",
        					"username": "kostyazicus",
        					"type": "private"
        				},
        				"date": 1704451336,
        				"text": "/start",
        				"entities": [
        					{
        						"offset": 0,
        						"length": 6,
        						"type": "bot_command"
        					}
        				]
        			}
        		}
        	]
        }
    """.trimIndent()

//    val word = Json.encodeToString(
//        Word(
//            original = "Hello",
//            translate = "Привет",
//            correctAnswersCount = 0,
//        )
//    )
//    println(word)
//
//    val wordObject = Json.decodeFromString<Word>(
//        """{"original": "Hello", "translate":  "Привет"}"""
//    )
//    println(wordObject)

//    val response = json.decodeFromString<Response>(responseString)
//    println(response)
}