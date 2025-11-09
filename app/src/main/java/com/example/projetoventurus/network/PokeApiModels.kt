package com.example.projetoventurus.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- INTERFACE DA API (RETROFIT) ---

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 20, // Paging 3 vai sobrepor isso
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    @GET("pokemon/{name}")
    suspend fun getPokemonDetail(@Path("name") name: String): PokemonDetailResponse

    @GET("type/{name}")
    suspend fun getPokemonByType(@Path("name") name: String): TypeDetailResponse
}

// --- INSTÂNCIA DO RETROFIT (SINGLETON) ---

object RetrofitInstance {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    val api: PokeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }
}

// --- CLASSES DE MODELO (DATA CLASSES) ---

// Resposta da lista principal
data class PokemonListResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("results") val results: List<PokemonResult>
)

// Item individual na lista
data class PokemonResult(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
) {
    fun getImageUrl(): String {
        val id = url.split("/").dropLast(1).last()
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }

    fun getId(): Int {
        return url.split("/").dropLast(1).last().toIntOrNull() ?: 0
    }
}


// Resposta dos detalhes do Pokémon
data class PokemonDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int,
    @SerializedName("types") val types: List<TypeSlot>,
    @SerializedName("stats") val stats: List<StatSlot>,
    @SerializedName("sprites") val sprites: Sprites,
    @SerializedName("cries") val cries: Cries // ◀️ ADICIONADO: Campo para os sons
)

// ◀️ ADICIONADO: Data class para os sons
data class Cries(
    @SerializedName("latest") val latest: String, // Som .ogg mais recente
    @SerializedName("legacy") val legacy: String? // Som .ogg antigo
)

// Classes aninhadas para Detalhes
data class TypeSlot(
    @SerializedName("type") val type: TypeInfo
)

data class TypeInfo(
    @SerializedName("name") val name: String
)

data class StatSlot(
    @SerializedName("base_stat") val baseStat: Int,
    @SerializedName("stat") val stat: StatInfo
)

data class StatInfo(
    @SerializedName("name") val name: String
)

data class Sprites(
    @SerializedName("other") val other: OtherSprites
)

data class OtherSprites(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtwork
)

data class OfficialArtwork(
    @SerializedName("front_default") val frontDefault: String
)

// Classes de modelo para a resposta da API de Tipo
data class TypeDetailResponse(
    @SerializedName("pokemon") val pokemon: List<TypePokemonSlot>
)

data class TypePokemonSlot(
    @SerializedName("pokemon") val pokemon: PokemonResult
)