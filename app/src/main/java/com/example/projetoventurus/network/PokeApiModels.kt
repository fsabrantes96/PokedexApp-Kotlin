package com.example.projetoventurus.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- INTERFACE DA API (RETROFIT) ---

interface PokeApiService {
    // Busca a lista paginada de Pokémon
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 151, // Padrão para Gen 1
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    // Busca detalhes de um Pokémon específico pelo nome
    @GET("pokemon/{name}")
    suspend fun getPokemonDetail(@Path("name") name: String): PokemonDetailResponse

    // ◀️ ADICIONADO: Busca Pokémon por tipo
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
    // Função auxiliar para extrair o ID da URL e montar a URL da imagem
    fun getImageUrl(): String {
        val id = url.split("/").dropLast(1).last()
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }

    // Função para extrair o ID
    fun getId(): Int {
        return url.split("/").dropLast(1).last().toIntOrNull() ?: 0
    }
}


// Resposta dos detalhes do Pokémon
data class PokemonDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("height") val height: Int, // Altura em decímetros
    @SerializedName("weight") val weight: Int, // Peso em hectogramas
    @SerializedName("types") val types: List<TypeSlot>,
    @SerializedName("stats") val stats: List<StatSlot>,
    @SerializedName("sprites") val sprites: Sprites
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
    @SerializedName("name") val name: String // ex: "hp", "attack", "defense"
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

// ◀️ ADICIONADO: Classes de modelo para a resposta da API de Tipo
data class TypeDetailResponse(
    // Contém a lista de todos os pokémons daquele tipo
    @SerializedName("pokemon") val pokemon: List<TypePokemonSlot>
)

data class TypePokemonSlot(
    // O objeto 'pokemon' aqui dentro tem 'name' e 'url',
    // exatamente como o nosso PokemonResult, então podemos reutilizá-lo!
    @SerializedName("pokemon") val pokemon: PokemonResult
)