plugins {
    application
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.kumo"

version = "0.0.2"

application {
    mainClass = "me.kumo.Widgets"
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("res")
    }
    test {
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
        java {
            java.srcDirs("test")
        }
    }
}
tasks.test {
    useJUnitPlatform()
}