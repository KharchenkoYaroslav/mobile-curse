# Модульний моноліт: Бізнес-модулі (`libs`)

Ця директорія призначена для зберігання внутрішньої бізнес-логіки бекенду, розділеної на окремі Gradle-модулі (архітектура **Modular Monolith**).

## Структура
- `libs/database` — модуль налаштування підключення до PostgreSQL та ORM (Spring Data JPA). Експортує залежності JPA, сканує пакети на наявність `@Entity` та `@Repository`.
- `libs/core` — базовий модуль бізнес-логіки (сервіси, реалізація спільних інтерфейсів з `:shared`).
- Можна додавати нові модулі за доменами (наприклад: `libs:auth`, `libs:monitoring`, `libs:reporting` тощо).

### Підключення до БД в інших бібліотеках:
Щоб модуль мав власні `@Entity` та `@Repository`:
1. Додайте плагін `alias(libs.plugins.kotlin.jpa)` у `build.gradle.kts` модуля.
2. Додайте залежність `implementation(project(":libs:database"))`.
3. Створюйте сутності `@Entity` та репозиторії `JpaRepository` у пакеті `ua.kpi.transformermonitoringsystem.<module>...`. Вони будуть автоматично знайдені та зареєстровані.

## Як додати новий модуль:
1. Створити підпапку, наприклад `libs/monitoring`.
2. Додати `build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.kotlin.jvm)
       alias(libs.plugins.kotlin.spring)
   }
   java {
       toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
   }
   dependencies {
       implementation(project(":shared"))
       implementation(platform(libs.spring.boot.dependencies))
       implementation("org.springframework.boot:spring-boot-starter")
   }
   ```
3. Зареєструвати в `settings.gradle.kts`:
   ```kotlin
   include(":libs:monitoring")
   ```
4. Підключити до `:server/build.gradle.kts`:
   ```kotlin
   implementation(project(":libs:monitoring"))
   ```
