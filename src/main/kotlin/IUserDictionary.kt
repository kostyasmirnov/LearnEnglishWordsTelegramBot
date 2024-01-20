interface IUserDictionary {

    fun getNumOfLearnedWords(): Int
    fun getSize(chatId: Long): Int
    fun getLearnedWords(chatId: Long): List<Word>
    fun getUnlearnedWords(chatId: Long): List<Word>
    fun setCorrectAnswersCount(word: String, correctAnswersCount: Int, chatId: Long)
    fun resetUserProgress(chatId: Long)
}