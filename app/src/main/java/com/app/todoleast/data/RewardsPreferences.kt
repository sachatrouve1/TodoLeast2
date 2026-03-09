package com.app.todoleast.data

import android.content.Context
import android.content.SharedPreferences
import com.app.todoleast.model.Achievement
import com.app.todoleast.model.RewardsState

class RewardsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "rewards_prefs",
        Context.MODE_PRIVATE
    )

    fun saveRewardsState(state: RewardsState) {
        prefs.edit().apply {
            putInt(KEY_TOTAL_POINTS, state.totalPoints)
            putInt(KEY_COMPLETED_TASKS_COUNT, state.completedTasksCount)
            putStringSet(KEY_UNLOCKED_ACHIEVEMENTS, state.unlockedAchievements.map { it.name }.toSet())
            putStringSet(KEY_REWARDED_TASK_IDS, state.rewardedTaskIds)
            apply()
        }
    }

    fun loadRewardsState(): RewardsState {
        val totalPoints = prefs.getInt(KEY_TOTAL_POINTS, 0)
        val completedTasksCount = prefs.getInt(KEY_COMPLETED_TASKS_COUNT, 0)
        val unlockedAchievementNames = prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet()) ?: emptySet()
        val rewardedTaskIds = prefs.getStringSet(KEY_REWARDED_TASK_IDS, emptySet()) ?: emptySet()

        val unlockedAchievements = unlockedAchievementNames.mapNotNull { name ->
            try {
                Achievement.valueOf(name)
            } catch (e: IllegalArgumentException) {
                null
            }
        }.toSet()

        return RewardsState(
            totalPoints = totalPoints,
            completedTasksCount = completedTasksCount,
            unlockedAchievements = unlockedAchievements,
            newlyUnlockedAchievement = null,
            rewardedTaskIds = rewardedTaskIds
        )
    }

    companion object {
        private const val KEY_TOTAL_POINTS = "total_points"
        private const val KEY_COMPLETED_TASKS_COUNT = "completed_tasks_count"
        private const val KEY_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements"
        private const val KEY_REWARDED_TASK_IDS = "rewarded_task_ids"
    }
}
