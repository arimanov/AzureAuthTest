plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    
    implementation("com.microsoft.azure:msal4j:1.13.10")
    implementation ("com.microsoft.graph:microsoft-graph:5.74.0")
    
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    
}

tasks.test {
    useJUnitPlatform()
}