fun main() {

    val trainer = try {
        LearnWordsTrainer(3, 4)

    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }


    fun Question.asConsoleString(): String {
        val variants = this.variants
            .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translate}" }
            .joinToString(separator = "\n")
        return this.correctAnswer.original + "\n" + variants + "\n" + "0 - выйти в меню"
    }

    while (true) {
        println(START_MESSAGE)
        val inputForStartMessage: Int = readln().toInt()
        when (inputForStartMessage) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println(LEARNED_ALL_WORDS)
                        break
                    } else {
                        println(question.asConsoleString())
                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == 0) break

                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {

                            println("Верно!\n")
                        } else {
                            println("Неверно. ${question.correctAnswer.original} - это ${question.correctAnswer.translate}\n")
                        }
                    }

                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percentLearned}%")
            }

            0 -> break
            else -> println(WARNING_MESSAGE)
        }
    }

}

const val START_MESSAGE = "Меню:\n1 - Учить слова\n2 - Статистика\n0 - Выход"
const val WARNING_MESSAGE = "Для выбора пункта из меню, введите цифру"
const val LEARNED_ALL_WORDS = "Вы выучили все слова"