# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontshrink
-optimizationpasses 5
-dontusemixedcaseclassnames#混淆时不会大小写混合类名
-dontskipnonpubliclibraryclasses #指定不去忽略非公共的库类
-dontpreverify #不预校验
-dontwarn #不警告
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/* #优化配置
-dontoptimize #不优化
-ignorewarnings #忽略警告
-repackageclasses me.ele

-keep class !me.ele.**, !retrofit2.**{*;}

# 保留签名，解决泛型、类型转换的问题
-keepattributes Signature
-keepattributes Exceptions
# 不混淆带有 annotation 的变量 和 函数
-keepattributes *Annotation*
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-keepclassmembers,allowoptimization enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class me.ele.mess.TestService$InnerService {
   *;
}

