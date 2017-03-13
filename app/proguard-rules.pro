# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/apple/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:

-keepclassmembers class android.luna.net.videohelper.video.parser.IqiyiParser$*{
    *;
}

-keepclassmembers class android.luna.net.videohelper.Ninja.View.NinjaWebView$*{
    *;
}

-optimizationpasses 5

-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!method/removal/*

-ignorewarning

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-dontwarn android.support.v7.**
-dontwarn android.support.v13.**
-dontwarn android.annotation

-keepattributes LineNumberTable

-keep class android.support.v7.**{*;}
-keep class android.support.v13.**{*;}
-keepattributes *Annotation*

#============================================
#对于引用第三方包的情况，可以采用下面方式避免打包出错：
-dontwarn tv.danmaku.ijk.media.player.**
-keep class tv.danmaku.ijk.media.player.** { *; }
-keep interface tv.danmaku.ijk.media.player.* { *; }
-dontwarn tv.danmaku.ijk.media.player_armv7a.**
-keep class tv.danmaku.ijk.media.player_armv7a.** { *;}
#umeng
-keep class com.umeng.**{*;}


-keep class **.R$*{*;}


-keepattributes Signature
-keep class cn.bmob.v3.** {*;}

# 保证继承自BmobObject、BmobUser类的JavaBean不被混淆
-keep class android.luna.net.videohelper.bean.user.Identification{*;}
-keep class android.luna.net.videohelper.bean.HotWord{*;}
-keep class android.luna.net.videohelper.bean.OlParam{*;}
-keep class android.luna.net.videohelper.bean.Recommend{*;}
-keep class android.luna.net.videohelper.bean.CloudVideo{*;}
-keep class android.luna.net.videohelper.bean.TvLife{*;}
-keep class android.luna.net.videohelper.bean.LeCloudSource{*;}

-keep class com.nostra13.universalimageloader.** {*;}
-dontwarn com.nostra13.universalimageloader.*

-dontwarn com.nineoldandroids.*

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-keep interface com.squareup.okhttp.** { *; }
-dontwarn okio.**

-keep class com.tencent.stat.**  {* ;}
-keep class com.tencent.mid.**  {* ;}

-keep class com.tencent.mm.sdk.** {*; }
-keep class android.luna.net.videohelptools.wxapi.** {*;}

# -> 所有native的方法不能去混淆.
-keepclasseswithmembernames class * {
    native <methods>;
}

#-->某些构造方法不能去混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# -> 枚举类不能去混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#    -> aidl文件不能去混淆.
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}



-dontwarn com.ut.mini.**
-dontwarn okio.**
-dontwarn com.xiaomi.**
-dontwarn com.squareup.wire.**
-dontwarn android.support.v4.**

-keepattributes *Annotation*

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }

-keep class okio.** {*;}
-keep class com.squareup.wire.** {*;}
-keep class cn.com.video.venvy.** {*;}

-keep class com.umeng.message.protobuffer.* {
	 public <fields>;
         public <methods>;
}

-keep class com.umeng.message.* {
	 public <fields>;
         public <methods>;
}

-keep class org.android.agoo.** {*;}

-keep class org.android.spdy.** {*;}

-keep class io.vov.utils.** { *; }
-keep class io.vov.vitamio.** { *; }

-keep class android.luna.net.videohelper.receiver.MiPushMessageReceiver {*;}

-printmapping build/outputs/mapping/release/mapping.txt

-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.app.NotificationCompat**{
    public *;
}

-keep class com.baidu.** { public protected *;}