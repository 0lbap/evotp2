plugins {
    application
    id("com.gradleup.shadow") version "8.3.2"
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
    implementation("guru.nidi:graphviz-java:0.18.1")
    implementation("org.apache.logging.log4j:log4j-core:2.24.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "fr.umontpellier.evo.Start"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}