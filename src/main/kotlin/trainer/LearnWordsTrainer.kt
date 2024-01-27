package trainer

import trainer.model.Question
import trainer.model.Statistics

class LearnWordsTrainer(
    private val chatId: Long,
    private val iUserDictionary: IUserDictionary,
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
    ) {

    var question: Question? = null
    private var lastCurrentAnswer: Question? = null

    fun getStatistics(): Statistics {
        val learned = iUserDictionary.getNumOfLearnedWords(chatId)
        val total = iUserDictionary.getSize(chatId)
        val percentLearned = learned * 100 / total

        return Statistics(learned, total, percentLearned)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = iUserDictionary.getUnlearnedWords(chatId, 0).shuffled()
        if (notLearnedList.isEmpty()) return null
        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = iUserDictionary.getLearnedWords(chatId, 0).shuffled()
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
                iUserDictionary.setCorrectAnswersCount(
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
        iUserDictionary.resetUserProgress(chatId)
    }

}