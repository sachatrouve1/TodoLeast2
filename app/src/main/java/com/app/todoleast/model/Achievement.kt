package com.app.todoleast.model

enum class Achievement(
    val title: String,
    val description: String,
    val icon: String,
    val requiredPoints: Int
) {
    STARTER("Premier pas", "Atteindre 10 points", "🎯", 10),
    BEGINNER("Débutant", "Atteindre 50 points", "⭐", 50),
    PRODUCTIVE("Productif", "Atteindre 100 points", "🌟", 100),
    CHAMPION("Champion", "Atteindre 250 points", "🏆", 250),
    MASTER("Maître", "Atteindre 500 points", "👑", 500),
    LEGENDARY("Légendaire", "Atteindre 1000 points", "💎", 1000)
}
