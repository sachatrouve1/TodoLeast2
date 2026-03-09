package com.app.todoleast.model

enum class Category(val label: String, val emoji: String) {
    NONE("Aucune", ""),
    WORK("Travail", "💼"),
    PERSONAL("Personnel", "🏠"),
    SHOPPING("Courses", "🛒"),
    HEALTH("Sante", "🏥"),
    EDUCATION("Etudes", "📚"),
    SPORT("Sport", "⚽"),
    OTHER("Autre", "📌")
}
