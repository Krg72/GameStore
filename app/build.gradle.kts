plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.krg.gamestore"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.krg.gamestore"
        minSdk = 24
        targetSdk = 34
        versionCode = 9
        versionName = "1.0.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    //implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-ads:22.6.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("org.apache.poi:poi:4.0.0")
    implementation("org.apache.poi:poi-ooxml:4.0.0")
    implementation("com.opencsv:opencsv:5.5.2")
    implementation("com.google.android.play:core:1.10.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

}