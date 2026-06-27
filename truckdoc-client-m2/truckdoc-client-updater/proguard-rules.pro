# R8 / ProGuard rules for the TruckDoc updater.
# minifyEnabled is false for the release build, so these are kept minimal.

-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Retrofit 1.x + Jackson models (reflection based).
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keep class com.sanda.truckdoc.client.updater.network.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-dontwarn rx.**
-dontwarn org.apache.commons.**
