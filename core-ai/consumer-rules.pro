# consumer-rules.pro
# These rules will be bundled with the AAR and applied to the app's build

# Keep AI Brain classes
-keep class com.algorithmx.androidmodules.coreai.brain.** { *; }

# Keep Hilt generated factories in core-ai
-keep class com.algorithmx.androidmodules.coreai.brain.di.** { *; }

# Keep Kotlin Serialization models
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.algorithmx.androidmodules.coreai.brain.models.** {
    *** Companion;
}
-keepnames class com.algorithmx.androidmodules.coreai.brain.models.**
-keepclassmembers class com.algorithmx.androidmodules.coreai.brain.models.** {
    @kotlinx.serialization.Serializable *;
}
