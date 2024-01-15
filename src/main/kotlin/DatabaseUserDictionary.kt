

class DatabaseUserDictionary {

    interface IUserDictionary {
        fun getNumOfLearnedWords(): Int
        fun getSize(): Int
        fun getLearnedWords(): List<String>
        fun getUnlearnedWords(chatId: Long): List<String>
        fun setCorrectAnswersCount(original: String, correctAnswersCount: Int)
        fun resetUserProgress(chatId: Long)
    }
}