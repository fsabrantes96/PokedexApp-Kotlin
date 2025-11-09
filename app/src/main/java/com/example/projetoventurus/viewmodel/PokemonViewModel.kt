package com.example.projetoventurus.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetoventurus.network.PokemonDetailResponse
import com.example.projetoventurus.network.PokemonResult
import com.example.projetoventurus.network.RetrofitInstance
import kotlinx.coroutines.launch

class PokemonViewModel : ViewModel() {

    // API
    private val apiService = RetrofitInstance.api

    // --- Lista Principal ---
    private val _pokemonListFiltred = MutableLiveData<List<PokemonResult>>()
    val pokemonListFiltred: LiveData<List<PokemonResult>> = _pokemonListFiltred

    // LiveData de Carregamento e Erro (Usado por ambas as telas)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Lista de backup para aplicar filtros
    private var originalList: List<PokemonResult> = emptyList()

    // --- Tela de Detalhes ---
    private val _pokemonDetail = MutableLiveData<PokemonDetailResponse>()
    val pokemonDetail: LiveData<PokemonDetailResponse> = _pokemonDetail


    // --- Funções ---

    /**
     * Busca a lista inicial de Pokémon (Geração I).
     */
    fun fetchPokemonList() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getPokemonList(limit = 151, offset = 0) // Gen 1
                originalList = response.results
                _pokemonListFiltred.postValue(originalList)
            } catch (e: Exception) {
                _error.postValue("Falha ao carregar dados: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Filtra a lista de Pokémon baseado no nome.
     */
    fun filterPokemonByName(query: String) {
        if (query.isEmpty()) {
            _pokemonListFiltred.postValue(originalList)
            return
        }

        val filtered = originalList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        _pokemonListFiltred.postValue(filtered)
    }

    /**
     * Filtra a lista de Pokémon baseado no tipo.
     */
    fun filterPokemonByType(typeName: String) {
        if (typeName == "all") {
            _pokemonListFiltred.postValue(originalList)
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getPokemonByType(typeName.lowercase())
                val namesFromTypeApi = response.pokemon.map { it.pokemon.name }.toSet()

                val filteredList = originalList.filter { pokemonGen ->
                    namesFromTypeApi.contains(pokemonGen.name)
                }
                _pokemonListFiltred.postValue(filteredList)

            } catch (e: Exception) {
                _error.postValue("Falha ao filtrar por tipo: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Busca uma nova lista de Pokémon com base na Geração.
     */
    fun filterPokemonByGeneration(generationName: String) {
        val (limit, offset) = when (generationName) {
            "Geração I" -> 151 to 0
            "Geração II" -> 100 to 151
            "Geração III" -> 135 to 251
            "Geração IV" -> 107 to 386
            "Geração V" -> 156 to 493
            else -> 151 to 0 // Padrão: Geração I
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getPokemonList(limit = limit, offset = offset)
                originalList = response.results
                _pokemonListFiltred.postValue(originalList)
            } catch (e: Exception) {
                _error.postValue("Falha ao carregar Geração: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    /**
     * Busca os detalhes de um Pokémon específico.
     */
    fun fetchPokemonDetail(name: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val detail = apiService.getPokemonDetail(name.lowercase())
                _pokemonDetail.postValue(detail)
            } catch (e: Exception) {
                _error.postValue("Falha ao carregar detalhes: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}