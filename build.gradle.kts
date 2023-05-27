//import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
// Commented out for now because plugin is incompatible with Wire (uses an old version of okio)
    //    alias(libs.plugins.benManes.versions)

    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
}

//tasks.withType<DependencyUpdatesTask> {
//    rejectVersionIf {
//        val reject = setOf("alpha", "beta", "rc", "dev")
//        reject.any { candidate.version.contains("-$it", ignoreCase = true) }
//    }
//}

// `./gradlew dependencyUpdates` to see new dependency versions
