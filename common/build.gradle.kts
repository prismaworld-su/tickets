plugins {
    id("java-library")
}

tasks.jar {
    archiveBaseName.set("tickets-common")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}
