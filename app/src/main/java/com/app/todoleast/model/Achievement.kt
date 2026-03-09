package com.app.todoleast.model

enum class Achievement(
    val title: String,
    val description: String,
    val icon: String,
    val requiredCount: Int
) {
    FIRST_TASK("Premier pas", "Completer votre premiere tache", "🎯", 1),
    FIVE_TASKS("En route", "Completer 5 taches", "⭐", 5),
    TEN_TASKS("Productif", "Completer 10 taches", "🌟", 10),
    TWENTY_FIVE_TASKS("Champion", "Completer 25 taches", "🏆", 25),
    FIFTY_TASKS("Maitre", "Completer 50 taches", "👑", 50),
    HUNDRED_TASKS("Legendaire", "Completer 100 taches", "💎", 100)
}
