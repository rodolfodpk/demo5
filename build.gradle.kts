plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("org.jetbrains.kotlin.kapt") version "1.5.0"
    id("io.micronaut.application") version "1.5.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.google.cloud.tools.jib") version "2.8.0"
}

version = "0.1"
group = "com.example1"

val kotlinVersion = project.properties.get("kotlinVersion")
val natsStreamingVersion = "2.2.3"
val vertxVersion = "4.1.0"
val crabzillaVersion = "v0.1.8"
val kotlinSerializationVersion = "1.2.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io" )
}

micronaut {
    runtime("netty")
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("com.example1.*")
    }
}

dependencies {
    kapt("io.micronaut.openapi:micronaut-openapi")
    implementation("io.micronaut:micronaut-http-client")
//    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
//    implementation("io.micronaut.sql:micronaut-vertx-pg-client")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("javax.annotation:javax.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("io.nats:java-nats-streaming:$natsStreamingVersion")

    implementation("io.github.crabzilla.crabzilla:crabzilla-core:$crabzillaVersion")
    implementation("io.github.crabzilla.crabzilla:crabzilla-pg-client:$crabzillaVersion")

//    implementation("io.github.crabzilla:crabzilla-core:$crabzillaVersion")
//    implementation("io.github.crabzilla:crabzilla-pg-client:$crabzillaVersion")

    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-pg-client:$vertxVersion")
    implementation("io.vertx:vertx-cassandra-client:$vertxVersion")
    implementation("io.vertx:vertx-auth-jwt:$vertxVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

}


application {
    mainClass.set("com.example1.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("11")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }


jib {
    to {
        image = "gcr.io/myapp/jib-image"
    }
}
}
