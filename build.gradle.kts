import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
  kotlin("jvm") version "2.4.0"
  id("com.google.devtools.ksp") version "2.3.10"
  id("com.gradleup.shadow") version "9.3.0"
}

val mindustryVersion = "v160.4"

kotlin {
  jvmToolchain(25)
  sourceSets {
    getByName("main") {
      kotlin.srcDirs("src")
      kotlin.srcDirs("build/generated/ksp/main/kotlin")
    }
  }
}

ksp {
  arg("min_game_version", mindustryVersion.substring(1).substringBefore("."))
}

repositories {
  mavenLocal()
  mavenCentral()
  maven { url = uri("https://www.jitpack.io") }
  ivy {
    name = "MindustryDeps"
    url = uri("https://gh-proxy.org/github.com/Anuken/")
    patternLayout {
      artifact("/Mindustry/releases/download/[revision]/dependencies.[ext]")
    }
    metadataSources { artifact() }
  }
  google()
}
dependencies {
  compileOnly("com.android.tools:r8:9.1.31")

  compileOnly(kotlin("stdlib-jdk8"))
  compileOnly("Anuken:Mindustry:$mindustryVersion")

  compileOnly("com.github.emmmM9O.mindustry-peroxicore:core:main-SNAPSHOT")
  compileOnly("com.github.emmmM9O.mindustry-peroxicore:ponder:main-SNAPSHOT")

  ksp("com.github.emmmM9O.mindustry-peroxicore:ksp:main-SNAPSHOT")
  kotlinCompilerPluginClasspath(
    "com.github.emmmM9O.mindustry-peroxicore:compiler-plugin:main-SNAPSHOT"
  )
}
tasks {
  withType<ShadowJar> {
    archiveFileName.set("${project.name}-desktop.jar")
    from("assets/") { include("**") }
  }

  register<JavaExec>("d8Compile") {
    dependsOn("shadowJar")

    doFirst {
      val r8Jar =
        configurations.compileClasspath.get().files.find {
          it.name.startsWith("r8-") && it.extension == "jar"
        } ?: throw GradleException("No R8.jar")

      classpath(files(r8Jar))
      mainClass.set("com.android.tools.r8.D8")

      val argsList = mutableListOf<String>()
      // argsList.add("--lib")
      // argsList.add(File(platformRoot, "android.jar").absolutePath)
      configurations.compileClasspath.get().files.forEach { file ->
        if (
          file.isFile &&
          file.extension == "jar" &&
          !file.name.startsWith("r8-") // && file.name.startsWith("Mindustry")
        ) {
          argsList.add("--classpath")
          argsList.add(file.absolutePath)
        }
      }

      argsList.add("--min-api")
      argsList.add("26")
      argsList.add("--output")
      argsList.add("${project.name}-android.jar")
      argsList.add("${project.name}-desktop.jar")

      args = argsList
      workingDir =
        layout.buildDirectory
          .dir("libs")
          .get()
          .asFile
    }
  }
  register<Jar>("deploy") {
    group = "deployment"
    dependsOn("d8Compile")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${project.name}.jar")
    from(
      zipTree("${layout.buildDirectory.get()}/libs/${project.name}-desktop.jar"),
      zipTree("${layout.buildDirectory.get()}/libs/${project.name}-android.jar")
    )
  }
}
