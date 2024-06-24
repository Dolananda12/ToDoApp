// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    kotlin("kapt") version "1.9.0" apply false
    val room_version = "2.6.1"
    id("androidx.room") version room_version apply false

}