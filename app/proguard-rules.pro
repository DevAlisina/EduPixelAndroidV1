# ProGuard / R8 rules for EduPixel
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Kotlinx Serialization
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking @kotlinx.serialization.Serializable class * { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions
