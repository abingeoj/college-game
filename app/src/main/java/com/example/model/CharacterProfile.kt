package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class CharacterId {
    SWAGGER,
    RUSHER,
    CHAMP,
    CHASER
}

enum class SpecialAbility {
    SWAGGER_DASH,   // Extended slide, quick recovery, agility boost
    SCHOOL_DASH,     // Speed boost & double multiplier on collecting books
    TROPHY_BOOST,    // Strong coin magnet & bonus CGPA
    STUDY_BOOST      // Auto-attracts books and formula sheets with shield
}

data class CharacterProfile(
    val id: CharacterId,
    val name: String,
    val title: String,
    val tagline: String,
    val description: String,
    val outfitDescription: String,
    val specialPowerName: String,
    val specialPowerDesc: String,
    val ability: SpecialAbility,
    val health: Int,        // 1 to 4
    val speed: Int,         // 1 to 5
    val magnet: Int,        // 1 to 5
    val unlockCost: Int,    // in Campus Coins (0 for defaults)
    val unlockGraduationsRequired: Int, // 0 for default
    @DrawableRes val imageRes: Int,
    val shirtColor: Long,
    val pantsColor: Long,
    val hasGlasses: Boolean = false,
    val hasSunglasses: Boolean = false,
    val hasCap: Boolean = false,
    val hasMustache: Boolean = true,
    val backpackColor: Long = 0xFF1E3A8A
) {
    companion object {
        val ALL_CHARACTERS = listOf(
            CharacterProfile(
                id = CharacterId.SWAGGER,
                name = "Srihari",
                title = "The Campus Legend",
                tagline = "Smooth. Stylish. Unstoppable.",
                description = "Campus trendsetter who balances sharp intellect with effortless swagger. Slides through engineering deadlines like a breeze.",
                outfitDescription = "Crisp open-collar white shirt, rolled-up sleeves, black slim trousers, and cool aviator shades.",
                specialPowerName = "Swagger Dash",
                specialPowerDesc = "Smooth agility boost with extended low slides and faster lane shifts!",
                ability = SpecialAbility.SWAGGER_DASH,
                health = 4,
                speed = 4,
                magnet = 3,
                unlockCost = 0,
                unlockGraduationsRequired = 0,
                imageRes = R.drawable.img_char_srihari,
                shirtColor = 0xFFF8FAFC,
                pantsColor = 0xFF1E293B,
                hasSunglasses = true,
                hasMustache = true,
                backpackColor = 0xFF0F172A
            ),
            CharacterProfile(
                id = CharacterId.RUSHER,
                name = "YouTuber",
                title = "Campus YouTuber",
                tagline = "Streaming the college hustle live to millions!",
                description = "High-energy campus creator sprinting between 8 AM lectures, campus vlog shoots, and viral tech challenges.",
                outfitDescription = "Red-and-blue plaid check creator streetwear, beige chinos, athletic sneakers, and signature baseball cap.",
                specialPowerName = "Viral Dash",
                specialPowerDesc = "Instant speed boost and 2x points when collecting books and lecture notes!",
                ability = SpecialAbility.SCHOOL_DASH,
                health = 3,
                speed = 5,
                magnet = 3,
                unlockCost = 0,
                unlockGraduationsRequired = 0,
                imageRes = R.drawable.img_char_youtuber,
                shirtColor = 0xFFB91C1C, // Plaid red accent
                pantsColor = 0xFFD4D4D8, // Light beige
                hasCap = true,
                hasMustache = true,
                backpackColor = 0xFF1E40AF
            ),
            CharacterProfile(
                id = CharacterId.CHAMP,
                name = "Rino",
                title = "Tech Ace Rino",
                tagline = "Code. Innovate. Graduate.",
                description = "Hackathon champion and algorithmic prodigy. Master of system design, AI circuits, and securing dream tech packages.",
                outfitDescription = "Pixel tech camo tee, rugged black cargo joggers, spectacles, and hackathon winner trophy.",
                specialPowerName = "Trophy Boost",
                specialPowerDesc = "Activates a powerful coin aura that pulls all nearby campus credits and boosts CGPA!",
                ability = SpecialAbility.TROPHY_BOOST,
                health = 4,
                speed = 4,
                magnet = 5,
                unlockCost = 150,
                unlockGraduationsRequired = 1,
                imageRes = R.drawable.img_char_rino,
                shirtColor = 0xFF18181B, // Black camo
                pantsColor = 0xFF27272A,
                hasGlasses = true,
                hasMustache = true,
                backpackColor = 0xFF0284C7
            ),
            CharacterProfile(
                id = CharacterId.CHASER,
                name = "Aditya",
                title = "Campus Chaser",
                tagline = "Smart, focused, and aiming for the top rank.",
                description = "Scholastic ace who never leaves anything for the last minute. High GPA and clean academic track record.",
                outfitDescription = "Peach polo shirt, blue rectangular rimmed spectacles, classic denim jeans, and heavy study backpack.",
                specialPowerName = "Study Boost",
                specialPowerDesc = "Attracts formula books, study notes, and coins with a protective study shield!",
                ability = SpecialAbility.STUDY_BOOST,
                health = 4,
                speed = 4,
                magnet = 4,
                unlockCost = 300,
                unlockGraduationsRequired = 2,
                imageRes = R.drawable.img_char_chaser,
                shirtColor = 0xFFFB923C, // Peach / salmon
                pantsColor = 0xFF1D4ED8, // Denim blue
                hasGlasses = true,
                hasMustache = true,
                backpackColor = 0xFF1E3A8A
            )
        )

        fun getById(id: CharacterId): CharacterProfile {
            return ALL_CHARACTERS.find { it.id == id } ?: ALL_CHARACTERS[0]
        }
    }
}
