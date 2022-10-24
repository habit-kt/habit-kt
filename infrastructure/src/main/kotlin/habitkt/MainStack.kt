package habitkt

import com.hashicorp.cdktf.App
import com.hashicorp.cdktf.TerraformStack
import com.hashicorp.cdktf.providers.azurerm.*
import software.amazon.jsii.Builder
import java.util.*

const val projectName = "example"

fun main() {
    val app = App()
    with(TerraformStack(app, projectName)) {

        (AzurermProvider.Builder.create(this, "AzureRm")) {
            features(AzurermProviderFeatures.builder().build())
        }

        val resourceGroup = (ResourceGroup.Builder.create(this, id())) {
            name("$projectName-resource-group")
            location("West Europe")
        }

        val appServicePlan = (AppServicePlan.Builder.create(this, id())) {
            name("$projectName-app-service-plan")
            location(resourceGroup.location)
            resourceGroupName(resourceGroup.name)
            sku(
                (AppServicePlanSku.builder()) {
                    tier("Standard")
                    size("S1")
                }
            )
        }

//        val appService = (AppService.Builder.create(this, id())) {
//            name("$projectName-app-service")
//            location(resourceGroup.location)
//            resourceGroupName(resourceGroup.name)
//            appServicePlanId(appServicePlan.id)
//            siteConfig(
//                (AppServiceSiteConfig.builder()) {
//                    dotnetFrameworkVersion("v4.0")
//                    scmType("LocalGit")
//                }
//            )
//            appSettings(mapOf("SOME_KEY" to "some-value"))
//        }

        val staticWebApp = (StaticSite.Builder.create(this, id())){
            name("$projectName-frontend")
            location(resourceGroup.location)
            resourceGroupName(resourceGroup.name)
        }

        // val calculate = (FunctionApp.Builder.create(this, id()))

    }
    app.synth()
}

fun id(): String = UUID.randomUUID().toString()

operator fun <R, B : Builder<R>> B.invoke(
    context: B.() -> Unit
): R = this.apply(context).build()