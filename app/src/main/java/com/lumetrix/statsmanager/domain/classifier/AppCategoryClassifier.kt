package com.lumetrix.statsmanager.domain.classifier

import com.lumetrix.statsmanager.domain.model.AppCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCategoryClassifier @Inject constructor() {

    private val productivePackages = setOf(
        "com.notion.id",
        "notion.id",
        "com.google.android.apps.docs",
        "com.microsoft.office.outlook",
        "com.slack",
        "com.todoist",
        "com.evernote",
        "com.google.android.calendar",
    )

    private val distractingPackages = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.snapchat.android",
        "com.twitter.android",
        "com.facebook.katana",
        "com.reddit.frontpage",
        "com.google.android.youtube",
    )

    fun classify(packageName: String): AppCategory = when {
        packageName in productivePackages -> AppCategory.Productive
        packageName in distractingPackages -> AppCategory.Distracting
        else -> AppCategory.Neutral
    }
}
