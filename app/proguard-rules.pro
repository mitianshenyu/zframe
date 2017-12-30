# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\android_sdk/tools/proguard/proguard-android.txt
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

#-dontobfuscate #不混淆
-dontshrink #不压缩，Shrinking默认为开，移除未被使用的类和成员，可以减小应用体积，并且在优化动作执行之后再执行,关闭压缩后第三方jar不会被打包到其中,否则会将libs/目录下的jar都合并到jar中去
#-dontoptimize #不优化
-optimizationpasses 5  #指定迭代优化的次数, 优化后看可以让应用运行更快,android一般为5,前提是优化没有被关闭
-dontusemixedcaseclassnames # 是否使用大小写混合
#-dontskipnonpubliclibraryclasses # 是否混淆第三方jar
#-dontpreverify # 混淆时是否做预校验
-verbose # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*    # 优化,混淆时所采用的算法
#混淆后默认会在工程目录app/build/outputs/mapping/release/下生成一个mapping.txt混淆规则文件,

-keepattributes *Annotation* #保留注解,如@Override
-keepattributes Signature #保留签名，如泛类签名<T>等
-keepattributes SourceFile,LineNumberTable #保留文件信息,方便调试
#-libraryjars libs/xxx.jar #导入jar库
#-libraryjars libs/armeabi/xxxx.so #导入so库
#-libraryjars libs/android-support-v4.jar
#-dontwarn android.support.v4.**{*;}

#不进行混淆保持原样
-keep public class * extends android.app.Activity                            # 保持不被混淆
-keep public class * extends android.app.Application                         # 保持不被混淆
-keep public class * extends android.app.Service                             # 保持不被混淆
-keep public class * extends android.content.BroadcastReceiver               # 保持不被混淆
-keep public class * extends android.content.ContentProvider                 # 保持不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper            # 保持不被混淆
-keep public class * extends android.preference.Preference                   # 保持不被混淆
-keep public class * extends android.os.IInterface{*;}                       # 保持AIDL不被混淆
-keep public class com.android.vending.licensing.ILicensingService           # 保持不被混淆

-keepclassmembers class **.R$* {#资源类变量需要保留
    public static <fields>;
}

-keepclasseswithmembernames class * {                                      # 保持 native 方法不被混淆
    native <methods>;
}

-keepclasseswithmembers class * {                                          # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);  # 保持自定义控件类不被混淆,
}

-keepclassmembers class * extends android.app.Activity {                   # 保持自定义控件类不被混淆
   public void *(android.view.View);
}

-keepclassmembers enum * {                                                 # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {                           # 保持 Parcelable 不被混淆
  public static final android.os.Parcelable$Creator *;
}

#不混淆第三方包中的指定内容
-keep class android-support-v4.**{*;}
-keep public class * extends android.support.v4.**
-keep class android.view.**{*;}

-keep class * extends com.zeu.frame.bind.Data {  # 保持所有共有类不被混淆
    public <fields>;
    public <methods>;

    protected <fields>;
    protected <methods>;
}

-keep class com.zeu.frame.** { #保持relies包下所有类的类名和共有成员函数及成员不被混淆
    public <fields>;
    public <methods>;

    protected <fields>;
    protected <methods>;
}

-keep class com.zeu.frame.bind.Bindloc {*;}
