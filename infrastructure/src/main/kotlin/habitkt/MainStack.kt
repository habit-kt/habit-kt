package habitkt

import com.hashicorp.cdktf.App
import com.hashicorp.cdktf.Fn
import com.hashicorp.cdktf.TerraformStack
import com.hashicorp.cdktf.providers.azurerm.app_service_plan.AppServicePlan
import com.hashicorp.cdktf.providers.azurerm.app_service_plan.AppServicePlanSku
import com.hashicorp.cdktf.providers.azurerm.container_registry.ContainerRegistry
import com.hashicorp.cdktf.providers.azurerm.cosmosdb_account.CosmosdbAccount
import com.hashicorp.cdktf.providers.azurerm.cosmosdb_account.CosmosdbAccountConsistencyPolicy
import com.hashicorp.cdktf.providers.azurerm.cosmosdb_account.CosmosdbAccountGeoLocation
import com.hashicorp.cdktf.providers.azurerm.function_app.FunctionApp
import com.hashicorp.cdktf.providers.azurerm.provider.AzurermProvider
import com.hashicorp.cdktf.providers.azurerm.provider.AzurermProviderFeatures
import com.hashicorp.cdktf.providers.azurerm.resource_group.ResourceGroup
import com.hashicorp.cdktf.providers.azurerm.static_site.StaticSite
import com.hashicorp.cdktf.providers.azurerm.storage_account.StorageAccount
import imports.azapi.resource.Resource
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

        val terraformStorage = (StorageAccount.Builder.create(this, id())) {
            name("${projectName}terraform")
            location(resourceGroup.location)
            resourceGroupName(resourceGroup.name)
            accountTier("Standard")
            accountReplicationType("LRS")
        }

        val calculateFunctionStorage = (StorageAccount.Builder.create(this, id())) {
            name("${projectName}calculatefunction")
            location(resourceGroup.location)
            resourceGroupName(resourceGroup.name)
            accountTier("Standard")
            accountReplicationType("LRS")
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


        val calculate = (FunctionApp.Builder.create(this, id())) {
            name("$projectName-calculate")
            location(resourceGroup.location)
            resourceGroupName(resourceGroup.name)
            appServicePlanId(appServicePlan.id)
            storageAccountName(calculateFunctionStorage.name)
            storageAccountAccessKey(calculateFunctionStorage.primaryAccessKey)
        }

        val cosmosdbAccount = (CosmosdbAccount.Builder.create(this, id())){
            name("$projectName-db")
            location(resourceGroup.location)
            resourceGroupName(resourceGroup.name)
            enableAutomaticFailover(false)
            kind("globalDocumentDb")
            consistencyPolicy ( (CosmosdbAccountConsistencyPolicy.builder()){
                consistencyLevel("Session")
            }
            )
            geoLocation(listOf((CosmosdbAccountGeoLocation.builder()){
                failoverPriority(0)
                location(resourceGroup.location)
            }))
        }

        val containerRegistry = (ContainerRegistry.Builder.create(this, id())) {
            name("$projectName")
            location(resourceGroup.location)
            resourceGroupName(resourceGroup.name)
            sku("Basic")
        }

        val containerAppsBackend  = (Resource.Builder.create(this, id())){
            name("$projectName")
            location(resourceGroup.location)
            parentId(resourceGroup.id)
            type("Microsoft.App/containerApps@2022-03-01")
            body(Fn.jsonencode("""
                {
                    properties: {
                    managedEnvironmentId = ${ }azapi_resource.managed_environment.id
                    configuration = {
                        ingress = {
                            external = true
                            targetPort =  80
                         }
                    }
                    template = {
                          containers      = [
                          {
                            image         = "habit-kt/backend:latest"
                            name          = "habit-kt-backend"
                            resources     = {
                              cpu         = 0.5
                              memory      = "1.0Gi"
                            }
                          },
                          {
                            image         = "habit-kt/export:latest"
                            name          = "habit-kt-export"
                            resources     = {
                              cpu         = 0.5
                              memory      = "1.0Gi"
                            }
                          }
                          ]
                          scale           = {
                            minReplicas   = 1
                            maxReplicas   = 1
                          }
                        }
                      },
                    }
                }
            """.trimIndent()))
        }

    }
    app.synth()
}

fun id(): String = UUID.randomUUID().toString()

operator fun <R, B : Builder<R>> B.invoke(
    context: B.() -> Unit
): R = this.apply(context).build()