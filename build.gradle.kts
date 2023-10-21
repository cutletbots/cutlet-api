plugins {
    id("java-library")
    id("maven-publish")
}

group = "ru.cutletbots"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {

    api("com.github.BlcDragon:objconfig:2.0.0")
    api("it.unimi.dsi:fastutil:8.2.2")

    //annotations
    api("org.jetbrains:annotations:22.0.0")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testCompileOnly("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    //logging
    api("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.7")
    implementation("org.slf4j:jcl-over-slf4j:1.7.32")
    implementation("org.slf4j:log4j-over-slf4j:1.7.32")
    implementation("uk.org.lidalia:sysout-over-slf4j:1.0.2")

    //tests
    testImplementation("junit:junit:4.13.2")

}

val fatJar = task("cutletRunnable", type = Jar::class) {
    group = "get-jar"
    archiveBaseName.set("${project.name}-shaded")
    manifest {
        attributes["Main-Class"] = "ru.blc.cutlet.api.Start"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }

    artifacts {
        archives(sourcesJar)
        archives(jar)
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Test> {
    useJUnit()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "16"
    targetCompatibility = "16"
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/cutletbots/cutlet-api")
            credentials {
                username = System.getenv("PACKAGE_REGISTRY_USERNAME")
                password = System.getenv("PACKAGE_REGISTRY_TOKEN")
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}