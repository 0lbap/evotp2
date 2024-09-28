plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.2"
}

group = "fr.umontpellier"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.eclipse.tycho.core)
    api(libs.eclipse.core.runtime)
    api(libs.eclipse.burt.resources)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}