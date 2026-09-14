plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
}

dependencies {
    implementation(project(":tickets-common"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
