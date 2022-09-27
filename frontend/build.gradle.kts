plugins {
    kotlin("js")
}

kotlin {
    js {
        browser()
        binaries.executable()
    }
}

dependencies {
    implementation(project(":model"))

    // fritz2
    implementation("dev.fritz2:core:${project.ext["fritz2Version"]}")

    // tailwind
    implementation(npm("tailwindcss", "3.0.19"))
    implementation(npm("@tailwindcss/forms", "0.4.0"))

    // webpack
    implementation(devNpm("postcss", "8.4.6"))
    implementation(devNpm("postcss-loader", "6.2.1"))
    implementation(devNpm("autoprefixer", "10.4.2"))
    implementation(devNpm("css-loader", "6.6.0"))
    implementation(devNpm("style-loader", "3.3.1"))
    implementation(devNpm("cssnano", "5.0.17"))
}