import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.sonar)
    id("jacoco")
    alias(libs.plugins.gms)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.github.se.travelpouch"
    compileSdk = 34
    compileSdk = 34

    // Load the API key from local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val keystoreFile = System.getenv("KEYSTORE_FILE") ?: localProperties.getProperty("KEYSTORE_FILE")
    val keystorePassword = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("KEY_ALIAS")
    val keyPassword = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("KEY_PASSWORD")
    val mapsApiKey: String = System.getenv("MAPS_API_KEY") ?: (localProperties.getProperty("MAPS_API_KEY") ?: "")
    var chosenConfig: Boolean = false // this is to avoid issues with the signing config when building apk

    defaultConfig {
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        applicationId = "com.github.se.travelpouch"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.github.se.travelpouch.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Declare the API key as a BuildConfig field
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")

        // Enable BuildConfig functionality
        buildFeatures {
            buildConfig = true
        }
    }

    buildTypes {
        release {
            //isMinifyEnabled = true
            //isShrinkResources = true
            //proguardFiles(
            //    getDefaultProguardFile("proguard-android-optimize.txt"),
            //    "proguard-rules.pro"
            //)
            // Only assign signing config if it exists
            if (keystoreFile != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
                println("creating a release config")
                chosenConfig = true // prevent debug signingConfig from getting created
                signingConfig = signingConfigs.create("release") {
                    storeFile(file(keystoreFile))
                    storePassword(keystorePassword)
                    keyAlias(keyAlias)
                    keyPassword(keyPassword)
                }
            }
            else {
                println("No release signing config set.")
            }
        }

        debug {
            if (keystoreFile != null && keystorePassword != null && keyAlias != null && keyPassword != null && !chosenConfig) {
                println("creating a debug config")
                signingConfig = signingConfigs.create("debug") {
                    storeFile(file(keystoreFile))
                    storePassword(keystorePassword)
                    keyAlias(keyAlias)
                    keyPassword(keyPassword)
                }
            }
            else {
                println("No debug signing config set.")
            }
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    testCoverage {
        jacocoVersion = "0.8.12"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // Robolectric needs to be run only in debug. But its tests are placed in the shared source set (test)
    // The next lines transfers the src/test/* from shared to the testDebug one
    //
    // This prevent errors from occurring during unit tests
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")

        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }

    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }
}

sonar {
    properties {
        property("sonar.projectKey", "TravelPouch_travelpouch")
        property("sonar.projectName", "Travel-Pouch")
        property("sonar.organization", "travelpouch")
        property("sonar.host.url", "https://sonarcloud.io")
        // Comma-separated paths to the various directories containing the *.xml JUnit report files. Each path may be absolute or relative to the project base directory.
        property("sonar.junit.reportPaths", "${project.layout.buildDirectory.get()}/test-results/testDebugunitTest/")
        // Paths to xml files with Android Lint issues. If the main flavor is changed, this file will have to be changed too.
        property("sonar.androidLint.reportPaths", "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml")
        // Paths to JaCoCo XML coverage report files.
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

// When a library is used both by robolectric and connected tests, use this function
fun DependencyHandlerScope.globalTestImplementation(dep: Any) {
    androidTestImplementation(dep)
    testImplementation(dep)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.play.services.mlkit.document.scanner)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.navigation.testing)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    globalTestImplementation(libs.androidx.junit)
    globalTestImplementation(libs.androidx.espresso.core)

    //Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.converter.scalars)

    // Google Service and Maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    implementation(libs.play.services.auth)

    // Firebase
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)

    // Networking with OkHttp
    implementation(libs.okhttp)

    // Testing Unit
    testImplementation(libs.junit)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    testImplementation(libs.json)
    testImplementation (libs.mockk.v1120)
    testImplementation(libs.androidx.core.testing)

    // Test UI
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.kaspresso)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.ui.test.junit4)


    // ------------- Jetpack Compose ------------------
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    globalTestImplementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    // Material Design 3
    implementation(libs.compose.material3)
    // Material Design 3 Icons Extended
    implementation(libs.compose.material.icons.extended)
    // Integration with activities
    implementation(libs.compose.activity)
    // Integration with ViewModels
    implementation(libs.compose.viewmodel)
    // Coil
    implementation(libs.coil)
    // Bouquet
    implementation(libs.bouquet)
    // Android Studio Preview support
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    // UI Tests
    globalTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)

    // --------- Kaspresso test framework ----------
    globalTestImplementation(libs.kaspresso)
    globalTestImplementation(libs.kaspresso.compose)

    // ----------       Robolectric     ------------
    testImplementation(libs.robolectric)

    // Test UI
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

tasks.withType<Test> {
    // Configure Jacoco for each tests
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )

    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.layout.projectDirectory}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
    // strange workaround to fix the 65535 issue
    // https://github.com/EPFLSWENT2024G1/partagix/pull/78 taken from this PR of a previous year
    doLast {
        val reportFile = reports.xml.outputLocation.asFile.get()
        val newContent = reportFile.readText().replace("<line[^>]+nr=\"65535\"[^>]*>".toRegex(), "")
        reportFile.writeText(newContent)

        logger.quiet("Wrote summarized jacoco test coverage report xml to $reportFile.absolutePath}")
    }
}

tasks.register("beforeCommitCheck") {
    dependsOn("ktfmtFormat", "ktfmtCheck")
}

tasks.named("ktfmtCheck") {
    mustRunAfter("ktfmtFormat")
}

tasks.named("ktfmtCheckAndroidTest") {
    dependsOn("ktfmtFormatAndroidTest")
    mustRunAfter("ktfmtFormatAndroidTest")
}

tasks.named("ktfmtCheckMain") {
    dependsOn("ktfmtFormatMain")
    mustRunAfter("ktfmtFormatMain")
}

tasks.named("ktfmtCheckTest") {
    dependsOn("ktfmtFormatTest")
    mustRunAfter("ktfmtFormatTest")
}
