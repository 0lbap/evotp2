plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.eclipse.tycho.core)
    api(libs.eclipse.core.runtime)
    api(libs.eclipse.burt.resources)

    api(project(":evo-common"))

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}