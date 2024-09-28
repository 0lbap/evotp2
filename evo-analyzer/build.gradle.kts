plugins {
    `java-library`
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

    implementation(libs.picocli.api)
    annotationProcessor(libs.picocli.annotation.processor)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}