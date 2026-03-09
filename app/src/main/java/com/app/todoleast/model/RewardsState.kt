package com.app.todoleast.model

data class RewardsState(
    val totalPoints: Int = 0,
    val completedTasksCount: Int = 0,
    val unlockedAchievements: Set<Achievement> = emptySet(),
    val newlyUnlockedAchievement: Achievement? = null
) {
    fun getPointsForPriority(priority: Priority): Int {
        return when (priority) {
            Priority.LOW -> 10
            Priority.MEDIUM -> 25
            Priority.HIGH -> 50
        }
    }

    fun checkNewAchievements(newCount: Int): Achievement? {
        return Achievement.entries
            .filter { it.requiredCount == newCount }
            .firstOrNull { it !in unlockedAchievements }
    }
}
