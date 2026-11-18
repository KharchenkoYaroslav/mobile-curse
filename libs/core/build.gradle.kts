plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))

    api(project(":libs:database"))
    api(project(":shared"))

    api(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.web)
}
