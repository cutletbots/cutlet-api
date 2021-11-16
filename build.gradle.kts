plugins {
    java
}

group = "ru.cutletbots"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {

    implementation("com.github.BlcDragon:objconfig:v1.0.0")

    //annotations
    implementation("org.jetbrains:annotations:22.0.0")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testCompileOnly("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    //logging
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    implementation("org.slf4j:jcl-over-slf4j:1.7.32")
    implementation("org.slf4j:log4j-over-slf4j:1.7.32")
    implementation("uk.org.lidalia:sysout-over-slf4j:1.0.2")

    //tests
    testImplementation("junit:junit:4.13.2")

}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-shaded")
    manifest {
        attributes["Main-Class"] = "ru.blc.cutlet.api.Start"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}

tasks.withType<Test> {
    useJUnit()
}