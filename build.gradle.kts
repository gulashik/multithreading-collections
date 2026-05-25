
plugins {
    java
}

group = "org.gulash.demo"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // SLF4J API для логирования
    implementation("org.slf4j:slf4j-api:2.0.13")
    // Logback - реализация логирования, используемая в проекте
    implementation("ch.qos.logback:logback-classic:1.5.6")
    
    // JUnit 5 для тестирования
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
