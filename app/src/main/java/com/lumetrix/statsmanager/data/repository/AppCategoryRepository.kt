package com.lumetrix.statsmanager.data.repository

import com.lumetrix.statsmanager.data.local.dao.AppCategoryDao
import com.lumetrix.statsmanager.data.local.entity.AppCategoryOverrideEntity
import com.lumetrix.statsmanager.domain.classifier.AppCategoryClassifier
import com.lumetrix.statsmanager.domain.model.AppCategory
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCategoryRepository @Inject constructor(
    private val appCategoryDao: AppCategoryDao,
    private val defaultClassifier: AppCategoryClassifier,
) {

    private val overrideCache = ConcurrentHashMap<String, AppCategory>()
    private var cacheLoaded = false

    suspend fun ensureCacheLoaded() {
        if (cacheLoaded) return
        appCategoryDao.getAll().forEach { entity ->
            overrideCache[entity.packageName] = AppCategory.fromStorageKey(entity.category)
        }
        cacheLoaded = true
    }

    suspend fun resolveCategory(packageName: String): AppCategory {
        ensureCacheLoaded()
        overrideCache[packageName]?.let { return it }
        val stored = appCategoryDao.getByPackage(packageName)
        if (stored != null) {
            val category = AppCategory.fromStorageKey(stored.category)
            overrideCache[packageName] = category
            return category
        }
        return defaultClassifier.classify(packageName)
    }

    suspend fun setUserCategory(packageName: String, category: AppCategory) {
        ensureCacheLoaded()
        val now = System.currentTimeMillis()
        appCategoryDao.upsert(
            AppCategoryOverrideEntity(
                packageName = packageName,
                category = category.storageKey,
                source = "user",
                updatedAt = now,
            ),
        )
        overrideCache[packageName] = category
    }

    suspend fun cycleCategory(packageName: String): AppCategory {
        val current = resolveCategory(packageName)
        val next = when (current) {
            AppCategory.Productive -> AppCategory.Neutral
            AppCategory.Neutral -> AppCategory.Distracting
            AppCategory.Distracting -> AppCategory.Productive
        }
        setUserCategory(packageName, next)
        return next
    }

    fun observeCategoryChanges() = appCategoryDao.observeAll()
}
