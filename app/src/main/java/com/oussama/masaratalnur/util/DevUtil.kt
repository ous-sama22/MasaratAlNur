package com.oussama.masaratalnur.util

import android.util.Log
import com.oussama.masaratalnur.data.model.ContentBlock
import com.oussama.masaratalnur.data.model.ContentStatus
import com.oussama.masaratalnur.data.model.Lesson
import com.oussama.masaratalnur.data.repository.ContentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DevUtil {

    // Function to add sample lessons for a specific topic ID
    // IMPORTANT: This is for development/testing ONLY. Remove or secure properly for production.
    fun addSampleLessons(
        contentRepository: ContentRepository,
        coroutineScope: CoroutineScope // Pass scope for launching coroutines
    ) {
        // Create sample lessons (customize as needed)
        val sampleLessons = listOf(
            // === Lessons for Topic: "Short Surahs" (Example) ===
            // !! Make sure TARGET_TOPIC_ID below matches your actual Topic ID for "Short Surahs" !!
            Lesson(
                topicId = "cJjy3sfwxKYNAnHERe5t", // <<< CHANGE THIS
                title_ar = "الدرس 1: سورة الفاتحة - أم الكتاب",
                order = 1,
                contentBlocks = listOf(
                    ContentBlock(type = "header", value_ar = "أهمية سورة الفاتحة", order = 1),
                    ContentBlock(type = "text", value_ar = "تعتبر سورة الفاتحة ركنًا أساسيًا في الصلاة ولا تصح الصلاة بدونها.", order = 2),
                    ContentBlock(type = "quote", value_ar = "﴿بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ * الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ * ...﴾", order = 3),
                    ContentBlock(type = "text", value_ar = "تشمل السورة الثناء على الله وتمجيده وبيان ألوهيته وربوبيته.", order = 4)
                ),
                xpAward = 10,
                status = ContentStatus.PUBLISHED
            ),
            Lesson(
                topicId = "cJjy3sfwxKYNAnHERe5t", // <<< CHANGE THIS
                title_ar = "الدرس 2: سورة الإخلاص - التوحيد الخالص",
                order = 2,
                contentBlocks = listOf(
                    ContentBlock(type = "header", value_ar = "تعدل ثلث القرآن", order = 1),
                    ContentBlock(type = "text", value_ar = "أخبر النبي صلى الله عليه وسلم أن سورة الإخلاص تعدل ثلث القرآن في الأجر والفضل.", order = 2),
                    ContentBlock(type = "quote", value_ar = "﴿قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ﴾", order = 3),
                    ContentBlock(type = "text", value_ar = "تتضمن السورة إثبات وحدانية الله وأنه المقصود في قضاء الحوائج.", order = 4)
                ),
                xpAward = 10,
                status = ContentStatus.PUBLISHED
            ),
            Lesson(
                topicId = "cJjy3sfwxKYNAnHERe5t", // <<< CHANGE THIS
                title_ar = "الدرس 3: سورة الناس - الاستعاذة من الشرور",
                order = 3,
                contentBlocks = listOf(
                    ContentBlock(type = "text", value_ar = "هذه السورة تعلم المسلم كيف يستعيذ بالله من شر الوسواس الخناس.", order = 1),
                    ContentBlock(type = "reference", value_ar = "انظر تفسير السعدي لمعاني أعمق.", order = 2) // Example reference block
                ),
                xpAward = 5,
                status = ContentStatus.DRAFT // Example Draft Lesson
            ),

            // === Lessons for Topic: "Pillars of Faith" (Example) ===
            // !! Make sure TARGET_TOPIC_ID below matches your actual Topic ID for "Pillars of Faith" !!
            Lesson(
                topicId = "PyluGenNCAXI2vMWtzVQ", // <<< CHANGE THIS
                title_ar = "الدرس 1: الإيمان بالله",
                order = 1,
                contentBlocks = listOf(
                    ContentBlock(type = "header", value_ar = "أساس الإيمان", order = 1),
                    ContentBlock(type = "text", value_ar = "الإيمان بالله هو الركن الأول وأهم أركان الإيمان، ويتضمن الإيمان بوجوده وربوبيته وألوهيته وأسمائه وصفاته.", order = 2)
                ),
                xpAward = 15,
                status = ContentStatus.PUBLISHED
            ),
            Lesson(
                topicId = "PyluGenNCAXI2vMWtzVQ", // <<< CHANGE THIS
                title_ar = "الدرس 2: الإيمان بالملائكة",
                order = 2,
                contentBlocks = listOf(
                    ContentBlock(type = "text", value_ar = "يجب الإيمان بوجود الملائكة وأنهم خلق من خلق الله، لهم وظائف محددة، ومنهم جبريل وميكائيل وإسرافيل.", order = 1)
                ),
                xpAward = 10,
                status = ContentStatus.PUBLISHED
            )
        )
        // Launch coroutine to add lessons via repository
        coroutineScope.launch(Dispatchers.IO) { // Use IO dispatcher for Firestore writes
            var successCount = 0
            sampleLessons.forEach { lesson ->
                val result = contentRepository.addLesson(lesson)
                if (result.isSuccess) {
                    Log.d("DevUtil", "Successfully added lesson: ${lesson.title_ar}")
                    successCount++
                } else {
                    Log.e("DevUtil", "Failed to add lesson: ${lesson.title_ar}", result.exceptionOrNull())
                }
            }
            Log.d("DevUtil", "Finished adding sample lessons. Success count: $successCount / ${sampleLessons.size}")
            // Maybe show a Toast on the main thread? Needs context.
        }
    }
}