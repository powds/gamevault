# Proguard rules for GameVault

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep Media3
-keep class androidx.media3.** { *; }

# Keep app models
-keep class com.gamevault.domain.model.** { *; }
-keep class com.gamevault.data.local.** { *; }