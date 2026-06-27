# R8 / ProGuard rules for the TruckDoc client.
# Generated as part of the AGP 8 / targetSdk 35 migration.

# Keep metadata needed by reflection-based libraries (Jackson, Retrofit, RxJava).
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions

# --- Picasso 2.5.2 transitively references OkHttp 2.x (com.squareup.okhttp.*),
#     which is not on the classpath (we use OkHttp3 via Retrofit). The default
#     downloader is not used, so it is safe to suppress these warnings.
-dontwarn com.squareup.okhttp.**

# --- Jackson (data binding via reflection) ---
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**
-keepnames class com.fasterxml.jackson.** { *; }

# Keep all API model / DTO classes that Jackson (de)serializes by reflection.
-keep class com.sanda.truckdoc.client.api.** { *; }

# --- OrmLite (uses reflection on annotated fields) ---
-keep class com.j256.ormlite.** { *; }
-dontwarn com.j256.ormlite.**
-keepclassmembers class * {
    @com.j256.ormlite.field.DatabaseField <fields>;
    @com.j256.ormlite.field.ForeignCollectionField <fields>;
}

# --- Retrofit / OkHttp3 / Okio ---
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# --- RxJava 1.x ---
-dontwarn rx.**
-keep class rx.** { *; }

# --- Misc third-party that ship with broken/old references ---
-dontwarn javax.annotation.**
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**

# Keep enum helpers used reflectively.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
