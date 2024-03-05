package com.autiscope.model

import android.os.Parcelable
import com.autiscope.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Question(
    val id: Int,
    val type: QuestionType,
    val audio: Int? = null,
    val audios: List<Int>? = null,
    val images: List<Int>? = null,
    val video: Int? = null
) :
    Parcelable {
    companion object {
        fun getQuestions(): List<Question> {
            val questionsArr = mutableListOf<Question>()
            questionsArr.add(Question(1, QuestionType.VIDEO, video = R.raw.question1))
            questionsArr.add(Question(2, QuestionType.VIDEO, video = R.raw.question2))
            questionsArr.add(Question(3, QuestionType.VIDEO, video = R.raw.question3))
            questionsArr.add(Question(4, QuestionType.VIDEO, video = R.raw.question4))
            questionsArr.add(
                Question(
                    5,
                    QuestionType.HEDGEHOG,
                    video = R.raw.hedgehog_video,
                    audio = R.raw.follow
                )
            )
//

            questionsArr.add(
                Question(
                    6,
                    QuestionType.MATCHING,
                    audio = R.raw.connect_mom_with_child,
                    images = listOf<Int>(
                        R.drawable.ic_mom, R.drawable.ic_child
                    )
                )
            )
            questionsArr.add(
                Question(
                    7,
                    QuestionType.IMAGE_AUDIO,
                    audio = R.raw.what_is_animal_name,
                    images = listOf(R.drawable.ic_cat)
                )
            )
            questionsArr.add(
                Question(
                    8,
                    QuestionType.IMAGE_AUDIO,
                    audio = R.raw.what_is_animal_name,
                    images = listOf(R.drawable.ic_lion)
                )
            )
            questionsArr.add(
                Question(
                    9,
                    QuestionType.FIND_SIMILARITIES,
                    audios = listOf(R.raw.rememmber_images, R.raw.find_similar_images),
                    images = listOf(
                        R.drawable.ic_strawberry,
                        R.drawable.ic_apple,
                        R.drawable.ic_apple,
                        R.drawable.ic_strawberry
                    )
                )
            )

            questionsArr.add(
                Question(
                    10,
                    QuestionType.FIND_DIFFERENCES,
                    audio = R.raw.find_differences_between_images,
                    images = listOf(R.drawable.ic_bear)
                )
            )

            return questionsArr
        }

    }
}


enum class QuestionType {
    VIDEO,
    IMAGE_AUDIO,
    MATCHING,
    FIND_DIFFERENCES,
    FIND_SIMILARITIES,
    HEDGEHOG
}

