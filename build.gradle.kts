plugins {
    kotlin("jvm") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "com.github.monulo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io/")
    maven("https://papermc.io/repo/repository/maven-public")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    implementation("com.github.monun:kommand:0.7.+")
    implementation("com.github.monun:tap:3.4.0")
}
tasks {
    create<Copy>("copyToServer") {
        from(shadowJar)
        var dest = File(rootDir, ".server/plugins")
        if(File(rootDir, shadowJar.get().archiveFileName.get()).exists()) dest = File(dest, "update")
        into(dest)
    }
}
