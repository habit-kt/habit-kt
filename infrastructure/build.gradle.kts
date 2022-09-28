plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("habitkt.MainStackKt")
}

dependencies {
    implementation("com.hashicorp:cdktf:0.12.2")
    implementation("com.hashicorp:cdktf-provider-azurerm:2.0.17")
}