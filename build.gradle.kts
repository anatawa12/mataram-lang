import org.jetbrains.gradle.ext.ProjectSettings
import org.jetbrains.gradle.ext.TaskTriggersConfig
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    java
    idea
    kotlin("jvm") version "1.4.10"
    id("org.jetbrains.grammarkit") version "2020.2.1"
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.7"
    id("com.anatawa12.kotlinScriptRunner") version "2.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

sourceSets {
    main {
        java {
            srcDir("${project.buildDir}/generated/sources/jflex")
        }
    }
}

idea {
    module {
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.7.0")
    implementation(kotlin("script-runtime"))
}

//idea.project.conf

idea.project {
    (idea.project as ExtensionAware).extensions.getByName<ProjectSettings>("settings").apply {
        val taskTriggers = (this as ExtensionAware).extensions.getByName<TaskTriggersConfig>("taskTriggers")
        taskTriggers.apply {
            beforeSync("generatePerlLexer")
        }
    }
}

tasks {
    withType<KotlinCompile>().all {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn,kotlin.contracts.ExperimentalContracts"
    }

    val generatePerlLexer by creating(GenerateLexer::class) {
        // source flex file
        source = "src/JFlexLexer.flex"

        // target directory for lexer
        targetDir = "${project.buildDir}/generated/sources/jflex/" +
                "com/anatawa12/mataram/parser"

        // target classname, target file will be targetDir/targetClass.java
        targetClass = "com.anatawa12.mataram.parser.JFlexLexer"

        // if set, plugin will remove a lexer output file before generating new one. Default: false
        purgeOldFiles = true
    }

    compileKotlin.get().dependsOn(generatePerlLexer)

    val generateNodeClasses by creating(com.anatawa12.kotlinScriptRunner.KotlinScriptExec::class) {
        kotlinVersion = "1.4.10"
        script = "$projectDir/src/main/kotlin/com/anatawa12/mataram/ast/AstGen.kts"
        args = listOf(
            "$projectDir/src/main/kotlin/com/anatawa12/mataram/ast/NodeClasses.kt"
        )
    }

    compileKotlin.get().dependsOn(generateNodeClasses)
}
