# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Retrofit classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep OkHttp classes
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Keep Moshi classes
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-keepattributes Signature
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Keep data classes used in API models
-keep class com.sanda.truckdoc.client.api.** { *; }
-keep class com.sanda.truckdoc.updater.data.model.** { *; }

# Keep Timber classes
-keep class timber.log.** { *; }

# Keep Logback classes (but exclude problematic ones)
-keep class ch.qos.logback.** { *; }
-dontwarn ch.qos.logback.**
-dontwarn javax.management.**
-dontwarn javax.naming.**
-dontwarn org.codehaus.janino.**
-dontwarn org.joda.convert.**
-dontwarn sun.reflect.**

# Keep Joda Time classes
-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**

# Keep Jackson classes
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

# Keep Guava classes
-keep class com.google.common.** { *; }
-dontwarn com.google.common.**

# Keep Esperandro classes
-keep class de.devland.esperandro.** { *; }
-keep @de.devland.esperandro.annotations.Esperandro interface * { *; }

# Keep RxJava classes
-keep class io.reactivex.** { *; }
-dontwarn io.reactivex.**

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep custom model classes
-keep class com.sanda.truckdoc.** { *; }
-keep class com.sanda.checker.** { *; }
-keep class wei.mark.standout.** { *; }
-keep class app.camera.tdoc.** { *; }

# Keep data binding classes
-keep class * extends androidx.databinding.ViewDataBinding {
    public static ** inflate(...);
    public static ** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static ** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean, Object);
}

# Keep generated classes
-keep class * extends androidx.databinding.library.baseAdapters.BR { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep R classes
-keep class **.R$* {
    public static <fields>;
}

# Keep BuildConfig
-keep class **.BuildConfig { *; }

# Remove problematic Logback features for Android
-dontwarn ch.qos.logback.classic.jmx.**
-dontwarn ch.qos.logback.classic.servlet.**
-dontwarn ch.qos.logback.core.util.JNDIUtil
-dontwarn ch.qos.logback.core.joran.conditional.** 

# Suppress warnings for javax.persistence (used by ORMLite)
-dontwarn javax.persistence.**

# Keep ORMLite classes and suppress warnings
-keep class com.j256.ormlite.** { *; }
-dontwarn com.j256.ormlite.** 