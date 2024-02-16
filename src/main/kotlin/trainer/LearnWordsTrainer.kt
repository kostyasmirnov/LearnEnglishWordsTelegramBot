package trainer

import datastore.DEFAULT_LEARNING_THRESHOLD
import trainer.model.Question
import trainer.model.Statistics

class LearnWordsTrainer(
    private val chatId: Long,
    private val userDictionary: IUserDictionary,
    private val learnedAnswerCount: Int = DEFAULT_LEARNING_THRESHOLD,
    private val countOfQuestionWords: Int = 4,
) {

    var question: Question? = null
    private var lastCurrentAnswer: Question? = null

    fun getStatistics(): Statistics {
        val learned = userDictionary.getNumOfLearnedWords(chatId, learnedAnswerCount)
        val total = userDictionary.getSize(chatId)
        val percentLearned = learned * 100 / total

        return Statistics(learned, total, percentLearned)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = userDictionary.getUnlearnedWords(chatId, learnedAnswerCount).shuffled()
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = userDictionary.getLearnedWords(chatId, learnedAnswerCount).shuffled()
            notLearnedList.shuffled()
                .take(countOfQuestionWords) + learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()
        val correctAnswer = questionWords.random()
        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        lastCurrentAnswer = question
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        val correctAnswer = 1
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                userDictionary.setCorrectAnswersCount(
                    lastCurrentAnswer?.correctAnswer?.original.toString(),
                    correctAnswer,
                    chatId
                )
                true
            } else {
                false
            }
        } ?: false
    }

    fun resetProgress() {
        userDictionary.resetUserProgress(chatId)
    }

}