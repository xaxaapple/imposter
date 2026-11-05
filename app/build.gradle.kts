plugins { id("com.android.application") }
android {
    namespace = "com.example.imposter"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.imposter"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    signingConfigs {
        create("rel") {
            storeFile = file("imposter.jks"); storePassword = "imposter"
            keyAlias = "imposter"; keyPassword = "imposter"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("rel")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
