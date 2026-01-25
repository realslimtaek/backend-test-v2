tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

dependencies {
    implementation(projects.modules.domain)
    implementation(projects.modules.external)
    // Only need Spring annotations (@Service) for this module
    implementation("org.springframework:spring-context")
}
