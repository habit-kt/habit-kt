plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("habitkt.MainStackKt")
}

dependencies {
    implementation("com.hashicorp:cdktf:0.13.0")
    implementation("com.hashicorp:cdktf-provider-azurerm:3.0.15")
}