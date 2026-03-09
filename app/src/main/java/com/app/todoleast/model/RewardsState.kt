package com.app.todoleast.model

data class RewardsState(
    val totalPoints: Int = 0,
    val completedTasksCount: Int = 0,
    val unlockedAchievements: Set<Achievement> = emptySet(),
    val newlyUnlockedAchievement: Achievement? = null,
    val rewardedTaskIds: Set<String> = emptySet()
) {
    fun getPointsForPriority(priority: Priority): Int {
        return when (priority) {
            Priority.LOW -> 10
            Priority.MEDIUM -> 25
            Priority.HIGH -> 50
        }
    }

    fun checkNewAchievements(newTotalPoints: Int): Achievement? {
        return Achievement.entries
            .filter { it.requiredPoints <= newTotalPoints }
            .firstOrNull { it !in unlockedAchievements }
    }
}
