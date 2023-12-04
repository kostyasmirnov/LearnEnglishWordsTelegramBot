import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")

    for (i in wordsFile.readLines()) {
        println(i)
    }

}