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

    // Використовуємо api, щоб модулі-споживачі (libs:core, libs:auth тощо)
    // автоматично мали доступ до Jakarta Persistence (@Entity, @Id тощо) та Spring Data JPA
    api(libs.spring.boot.starter.data.jpa)
    api(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
