package com.lumetrix.statsmanager.domain.evaluator

import com.lumetrix.statsmanager.core.time.DateUtils
import com.lumetrix.statsmanager.data.local.dao.AppChainRuleDao
import com.lumetrix.statsmanager.data.local.dao.AppUsageDao
import com.lumetrix.statsmanager.domain.model.AppChainRule
import com.lumetrix.statsmanager.domain.model.ChainEvalResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Evaluates App Chain Rules to determine if a target app is accessible today.
 *
 * A rule is satisfied when the gate app has been used for at least [gateDurationMin]
 * minutes on the current day.
 */
@Singleton
class AppChainEvaluator @Inject constructor(
    private val appChainRuleDao: AppChainRuleDao,
    private val appUsageDao: AppUsageDao,
) {

    /** Check if the [targetPackage] is allowed to open today, per any active chain rules. */
    suspend fun evaluate(targetPackage: String): ChainEvalResult {
        val rule = appChainRuleDao.getRuleForTarget(targetPackage)
            ?: return ChainEvalResult(isAllowed = true)

        val todayKey = DateUtils.toDayKey(DateUtils.today())
        val gateUsageMs = appUsageDao.getUsageForDate(rule.gatePackage, todayKey)
        val gateUsageMin = (gateUsageMs / 60_000L).toInt()
        val requiredMin = rule.gateDurationMin

        val isSatisfied = gateUsageMin >= requiredMin
        val remainingMin = (requiredMin - gateUsageMin).coerceAtLeast(0)
        val progress = (gateUsageMin.toFloat() / requiredMin.toFloat()).coerceIn(0f, 1f)

        return ChainEvalResult(
            isAllowed = isSatisfied,
            gateAppName = rule.gateAppName,
            gateDurationMin = requiredMin,
            remainingMin = remainingMin,
            gateProgress = progress,
        )
    }

    /** Converts DB entities to domain models with live progress for the UI. */
    suspend fun enrichRulesWithProgress(entities: List<com.lumetrix.statsmanager.data.local.entity.AppChainRuleEntity>): List<AppChainRule> {
        val todayKey = DateUtils.toDayKey(DateUtils.today())
        return entities.map { entity ->
            val gateUsageMs = appUsageDao.getUsageForDate(entity.gatePackage, todayKey)
            val gateUsageMin = (gateUsageMs / 60_000L).toInt()
            val progress = (gateUsageMin.toFloat() / entity.gateDurationMin.toFloat()).coerceIn(0f, 1f)
            val remaining = (entity.gateDurationMin - gateUsageMin).coerceAtLeast(0)
            AppChainRule(
                id = entity.id,
                gatePackage = entity.gatePackage,
                gateAppName = entity.gateAppName,
                gateDurationMin = entity.gateDurationMin,
                targetPackage = entity.targetPackage,
                targetAppName = entity.targetAppName,
                isEnabled = entity.isEnabled,
                gateProgress = progress,
                remainingMin = remaining,
                isSatisfied = gateUsageMin >= entity.gateDurationMin,
            )
        }
    }
}
