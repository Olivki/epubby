rootProject.name = "epubby"

enableFeaturePreview("STABLE_PUBLISHING")

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when(requested.id.id) {
                "kotlin-multiplatform" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "kotlinx-serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}