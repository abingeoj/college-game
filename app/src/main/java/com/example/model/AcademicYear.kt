package com.example.model

enum class AcademicYearStage(
    val yearNumber: Int,
    val title: String,
    val moduleName: String,
    val challengeDescription: String,
    val milestoneDistance: Float, // distance where this module starts
    val endDistance: Float,        // distance where this module ends
    val bannerText: String,
    val themeColor: Long,
    val secondaryColor: Long
) {
    YEAR_1(
        yearNumber = 1,
        title = "1st Year: Freshman",
        moduleName = "Assignments & Foundations",
        challengeDescription = "Dodge late assignment submissions, attendance barricades & heavy textbooks!",
        milestoneDistance = 0f,
        endDistance = 1200f,
        bannerText = "COLLEGE CAMPUS - 1ST YEAR",
        themeColor = 0xFF2563EB, // Royal Blue
        secondaryColor = 0xFF60A5FA
    ),
    YEAR_2(
        yearNumber = 2,
        title = "2nd Year: Sophomore",
        moduleName = "Semester Exams & Arrears",
        challengeDescription = "Dodge tricky theory exam hurdles, sudden quizzes & avoid the dreaded Supply!",
        milestoneDistance = 1200f,
        endDistance = 2600f,
        bannerText = "SEMESTER EXAM HALL - 2ND YEAR",
        themeColor = 0xFFD97706, // Amber / Gold
        secondaryColor = 0xFFFBBF24
    ),
    YEAR_3(
        yearNumber = 3,
        title = "3rd Year: Junior",
        moduleName = "Capstone Projects & Hackathons",
        challengeDescription = "Jump over buggy hardware, slide past viva examiners & submit mini-projects!",
        milestoneDistance = 2600f,
        endDistance = 4200f,
        bannerText = "INNOVATION & LABS - 3RD YEAR",
        themeColor = 0xFF7C3AED, // Purple / Tech Violet
        secondaryColor = 0xFFA78BFA
    ),
    YEAR_4(
        yearNumber = 4,
        title = "4th Year: Senior",
        moduleName = "Internships & Placements",
        challengeDescription = "Sprint through aptitude cutoffs, crack technical interviews & secure your dream offer!",
        milestoneDistance = 4200f,
        endDistance = 6000f,
        bannerText = "CAMPUS PLACEMENT DRIVE - 4TH YEAR",
        themeColor = 0xFF059669, // Emerald Green
        secondaryColor = 0xFF34D399
    );

    fun getProgressFraction(currentDistance: Float): Float {
        if (currentDistance <= milestoneDistance) return 0f
        if (currentDistance >= endDistance) return 1f
        return (currentDistance - milestoneDistance) / (endDistance - milestoneDistance)
    }

    companion object {
        val TOTAL_GRADUATION_DISTANCE = 6000f

        fun getStageForDistance(distance: Float): AcademicYearStage {
            return when {
                distance < YEAR_2.milestoneDistance -> YEAR_1
                distance < YEAR_3.milestoneDistance -> YEAR_2
                distance < YEAR_4.milestoneDistance -> YEAR_3
                else -> YEAR_4
            }
        }
    }
}
