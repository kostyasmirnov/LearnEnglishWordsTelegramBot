package trainer

import trainer.model.Word

interface IUserDictionary {

    fun getNumOfLearnedWords(chatId: Long, learningThreshold: Int): Int
    fun getSize(chatId: Long): Int
    fun getLearnedWords(chatId: Long, learningThreshold: Int): List<Word>
    fun getUnlearnedWords(chatId: Long, learningThreshold: Int): List<Word>
    fun setCorrectAnswersCount(original: String, correctAnswersCount: Int, chatId: Long)
    fun resetUserProgress(chatId: Long)
}