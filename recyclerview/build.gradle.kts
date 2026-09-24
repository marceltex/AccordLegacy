/*
 * Copyright (C) 2023 Helixform
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.agp.kotlin)
}

android {
    namespace = "androidx.recyclerview"
    compileSdk = 37

    defaultConfig {
        minSdk = 19
        multiDexEnabled = true
    }

    buildTypes {
        release {
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("main") {
            res.directories += "res"
            res.directories += "res-public"
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(libs.androidx.annotation)
    //noinspection KtxExtensionAvailable
    api(libs.androidx.core)
    //noinspection KtxExtensionAvailable,GradleDependency
    implementation(libs.androidx.collection)
    api(libs.androidx.customview)
    implementation(libs.androidx.customview.poolingcontainer)

    constraints {
        implementation(libs.androidx.viewpager2)
    }
}
