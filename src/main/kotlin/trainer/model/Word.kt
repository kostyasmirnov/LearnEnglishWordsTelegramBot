package trainer.model

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val original: String,
    var translate: String,
    var correctAnswersCount: Int = 0,
)