plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-velocity") version "3.1.0"
}

dependencies {
    implementation(project(":tickets-common"))
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    runVelocity {
        velocityVersion("3.5.0-SNAPSHOT")
    }
}
