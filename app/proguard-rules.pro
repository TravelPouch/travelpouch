# Portions of this code were generated and or inspired by the help of GitHub Copilot or Chatgpt
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
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
# Firebase UI Auth rules
-keep class com.firebase.ui.auth.** { *; }
-keep class com.google.android.gms.auth.api.credentials.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# General rules for Google Play Services
-dontwarn com.google.android.gms.**
-keep class androidx.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes *Annotation*
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# JP2Decoder
-keep class com.gemalto.jp2.** { *; }
-dontwarn com.gemalto.jp2.JP2Decoder