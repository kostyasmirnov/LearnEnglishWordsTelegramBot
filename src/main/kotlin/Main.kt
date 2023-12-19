import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")
    val dictionary = mutableListOf<Word>()


    val lines: List<String> = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = line[2].toInt())
        dictionary.add(word)
    }

    dictionary.forEach { println(it) }

}

data class Word(
    val original: String,
    var translate: String,
    val correctAnswersCount: Int = 0,
)