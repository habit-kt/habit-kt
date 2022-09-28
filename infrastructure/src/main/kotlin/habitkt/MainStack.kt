package habitkt

import com.hashicorp.cdktf.App
import com.hashicorp.cdktf.TerraformStack
import com.hashicorp.cdktf.providers.azurerm.*
import software.constructs.Construct

class MainStack(scope: Construct, id: String) : TerraformStack(scope, id) {

    init {
        AzurermProvider.Builder.create(this, "AzureRm")
            .features(AzurermProviderFeatures.builder().build())
            .build()

        val rg = ResourceGroup.Builder.create(this, "$id-rg")
            .name("example-appserviceplan")
            .location("West Europe")
            .build()

        val asp = AppServicePlan.Builder.create(this, "$id-asp")
            .name("example-appserviceplan")
            .location(rg.location)
            .resourceGroupName(rg.name)
            .sku(
                AppServicePlanSku.builder()
                    .tier("Standard")
                    .size("S1")
                    .build()
            )
            .build()

        AppService.Builder.create(this, "$id-ap")
            .name("example-app-service")
            .location(rg.location)
            .resourceGroupName(rg.name)
            .appServicePlanId(asp.id)
            .siteConfig(
                AppServiceSiteConfig.builder()
                    .dotnetFrameworkVersion("v4.0")
                    .scmType("LocalGit")
                    .build()
            )
            .appSettings(mapOf("SOME_KEY" to "some-value"))
            .build()
    }
}

fun main() {
    val app = App()
    MainStack(app, "example")
    app.synth()
}