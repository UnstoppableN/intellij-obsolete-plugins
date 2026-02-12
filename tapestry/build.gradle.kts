import org.jetbrains.intellij.platform.gradle.TestFrameworkType

// IntelliJ Platform Gradle Plugin 2.x configuration
// See: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = "com.intellij"
version = "253.0.0"  // Matches IntelliJ 2025.3 (build 253)

// Required repositories for IntelliJ Platform Gradle Plugin 2.x
repositories {
  mavenCentral()
  
  // IntelliJ Platform artifacts repository
  intellijPlatform {
    defaultRepositories()
  }
}

// Plugin configuration using intellijPlatform extension (replaces deprecated intellij extension)
intellijPlatform {
  pluginConfiguration {
    // IDE version compatibility range
    ideaVersion {
      sinceBuild = "243"      // Minimum: IntelliJ 2024.3
      untilBuild = "253.*"    // Maximum: All 2025.3.x releases
    }
  }
  
  // Configure plugin verifier to test against target IDE versions
  pluginVerification {
    ides {
      recommended()  // Test against recommended IDE versions
    }
  }
}

// Generated source directory for parser/lexer
sourceSets.getByName("main") {
  java.srcDir("src/main/gen")
}

// Dependencies using IntelliJ Platform Gradle Plugin 2.x syntax
dependencies {
  // IntelliJ Platform dependencies
  intellijPlatform {
    // Target IDE: IntelliJ IDEA Ultimate 2025.3
    create("IU", "2025.3")
    
    // Required bundled plugins (must be explicitly declared in Plugin 2.x)
    bundledPlugin("com.intellij.java")        // Java PSI support (PsiClass, PsiMethod, etc.)
    bundledPlugin("com.intellij.javaee")      // Java EE/Jakarta EE support
    bundledPlugin("com.intellij.properties")  // Properties file support
    bundledPlugin("com.intellij.css")         // CSS support (optional)
    
    // Test framework dependencies
    testFramework(TestFrameworkType.Platform)      // IntelliJ Platform test framework
    testFramework(TestFrameworkType.Plugin.Java)   // Java plugin test framework
    
    // Groovy support for test cases
    testBundledPlugin("org.intellij.groovy")
    
    // Plugin verifier for compatibility checking
    pluginVerifier()
    
    // Code instrumentation tools (required for plugin development)
    instrumentationTools()
  }

  // Runtime dependencies
  implementation("commons-chain:commons-chain:1.2")

  // Test dependencies
  testImplementation("junit:junit:4.13.2")              // JUnit 4 (primary test framework)
  testImplementation("org.testng:testng:7.10.2")        // TestNG (legacy tests)
  testImplementation("org.easymock:easymock:5.4.0")     // Mocking framework
  testImplementation("org.objenesis:objenesis:3.4")     // Object instantiation
  testImplementation("org.xmlunit:xmlunit-core:2.10.0") // XML comparison
  testImplementation("org.xmlunit:xmlunit-matchers:2.10.0")
}

// Task configuration
tasks {
  // Java compilation settings
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  
  // Use JUnit as primary test framework
  test {
    useJUnit()
  }
}