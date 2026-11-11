# Vocab9000 release R8 / ProGuard rules
# Keep only what shrinking would break (reflection / codegen). Do not keep the whole app.

# Attributes needed for Gson TypeToken generics, Room, and annotations
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin metadata (Room / coroutines / reflection helpers)
-keep class kotlin.Metadata { *; }

# --- Room ---
# Entities are read/written by generated code; field names must stay stable.
-keep class com.jianwang.vocab9000.data.entity.** { *; }
-keep class com.jianwang.vocab9000.data.db.Converters { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Gson ---
# DefinitionService deserializes Free Dictionary API JSON via TypeToken.
# CatalogStore parses longman9000.json with JsonParser (no POJO keep required).
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.jianwang.vocab9000.data.repository.DefinitionService$FreeDictionaryResponse { *; }
-keep class com.jianwang.vocab9000.data.repository.DefinitionService$FreeDictionaryResponse$* { *; }
-dontwarn sun.misc.**
-dontwarn com.google.gson.internal.ReflectionAccessFilter$FilterResult

# --- Domain ---
# CatalogEntry / StudyItem / SRSState are ordinary Kotlin types (not Gson-mapped).
# SRSGrade is an enum used from app code. Keep members so shrinking cannot
# drop values()/valueOf() if a call path is only visible at runtime.
-keepclassmembers enum com.jianwang.vocab9000.domain.SRSGrade {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
}

# --- Library consumer rules cover the rest ---
# Coroutines, DataStore Preferences, and WorkManager ship their own consumer
# ProGuard files. This app currently uses SharedPreferences (not DataStore)
# and does not enqueue WorkManager workers; no extra keep rules are added.
# Jetpack Compose is generally R8-safe with the default Android optimize file.
# There are no Parcelable / @Parcelize types in this app.
