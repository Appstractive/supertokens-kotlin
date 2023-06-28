package com.supertokens.sdk

import com.supertokens.sdk.recipes.Recipe
import com.supertokens.sdk.recipes.BuildRecipe
import com.supertokens.sdk.recipes.RecipeBuilder
import com.supertokens.sdk.recipes.RecipeConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

data class AppConfig(
    val name: String,
    val websiteDomain: String,
    val websiteBasePath: String? = null,
    val apiDomain: String,
    val apiBasePath: String? = null,
    val apiGatewayPath: String? = null,
)

fun <C: RecipeConfig, R: Recipe<C>> SuperTokensConfig.recipe(builder: RecipeBuilder<C, R>, configure: C.() -> Unit = {}) {
    +builder.install(configure)
}

class SuperTokensConfig(
    val connectionURI: String,
    val appConfig: AppConfig,
) {

    var client: HttpClient? = null

    var apiKey: String? = null

    var recipes: List<BuildRecipe> = emptyList()
        private set

    operator fun BuildRecipe.unaryPlus() {
        recipes = recipes + this
    }

}

class SuperTokens(
    private val config: SuperTokensConfig,
) {

    val recipes: List<Recipe<*>> = config.recipes.map { it.invoke(this) }

    val appConfig: AppConfig = config.appConfig

    @OptIn(ExperimentalSerializationApi::class)
    val client = config.client ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(Logging)

        defaultRequest {
            url(config.connectionURI)

            config.apiKey?.let {
                header(Constants.HEADER_API_KEY, it)
            }

            header(Constants.HEADER_CDI_VERSION, Constants.CDI_VERSION)
            contentType(ContentType.Application.Json)
        }
    }

    inline fun <reified T : Recipe<*>> getRecipe(): T = recipes.filterIsInstance<T>().firstOrNull()
        ?: throw RuntimeException("Recipe ${T::class.java.simpleName} not configured")

}

fun superTokens(connectionURI: String, appConfig: AppConfig, init: SuperTokensConfig.() -> Unit): SuperTokens {
    val config = SuperTokensConfig(
        connectionURI = connectionURI,
        appConfig = appConfig,
    ).apply(init)
    return SuperTokens(config)
}