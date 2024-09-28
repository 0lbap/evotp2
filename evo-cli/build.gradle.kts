plugins {
    id("java")
}

group = "fr.umontpellier"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":evo-analyzer"))

    implementation(libs.picocli.api)
    annotationProcessor(libs.picocli.annotation.processor)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}