package com.example.projetoventurus.ui

import android.app.ActivityOptions // ◀️ Import necessário
import android.content.Intent
import android.os.Bundle
import android.view.View // ◀️ Import necessário
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projetoventurus.R
import com.example.projetoventurus.adapter.PokemonAdapter
import com.example.projetoventurus.databinding.ActivityMainBinding
import com.example.projetoventurus.viewmodel.PokemonViewModel

// Data class para os Tipos (tradução)
data class PokemonType(val displayName: String, val apiKey: String)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: PokemonViewModel by viewModels()
    private lateinit var pokemonAdapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView() // ◀️ Chamada única
        setupObservers()
        setupSearchView()
        setupFilters()

        if (savedInstanceState == null) {
            viewModel.fetchPokemonList()
        }
    }

    //
    // ESTA É A ÚNICA E CORRETA VERSÃO DA FUNÇÃO
    //
    private fun setupRecyclerView() {
        // O lambda agora recebe (pokemon, view) da classe PokemonAdapter
        pokemonAdapter = PokemonAdapter { pokemon, imageView ->

            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("POKEMON_NAME", pokemon.name)

            // --- A MÁGICA DA ANIMAÇÃO ---

            // 1. Cria as opções de transição
            val options = ActivityOptions
                .makeSceneTransitionAnimation(this, imageView, "pokemon_image_transition")

            // 2. Inicia a activity com as opções
            startActivity(intent, options.toBundle())
        }

        binding.rvPokemonList.apply {
            adapter = pokemonAdapter
            layoutManager = GridLayoutManager(this@MainActivity, 2)
        }
    }

    private fun setupObservers() {
        // Observa a lista filtrada
        viewModel.pokemonListFiltred.observe(this) { list ->
            pokemonAdapter.submitList(list)
            binding.tvEmptyList.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observa o status de carregamento
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observa erros
        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterPokemonByName(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterPokemonByName(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupFilters() {

        // --- 1. Criar a lista de tipos usando as traduções ---
        val apiTypeKeys = listOf(
            "all", "fire", "water", "grass", "electric", "bug", "poison",
            "normal", "flying", "ground", "fairy", "fighting", "psychic",
            "rock", "ghost", "ice", "dragon"
        )

        val pokemonTypesList = apiTypeKeys.map { key ->
            val resId = resources.getIdentifier("type_$key", "string", packageName)
            val translatedName = getString(resId)
            PokemonType(displayName = translatedName, apiKey = key)
        }

        // --- 2. Criar a lista de gerações ---
        val geracoes = listOf(
            "Geração I", "Geração II", "Geração III", "Geração IV", "Geração V"
        )

        // --- 3. Criar os Adapters ---
        val typeDisplayNames = pokemonTypesList.map { it.displayName }
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, typeDisplayNames)
        val generationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, geracoes)

        // --- 4. Conectar os Adapters ---
        binding.autoCompleteType.setAdapter(typeAdapter)
        binding.autoCompleteGeneration.setAdapter(generationAdapter)

        // --- 5. Definir valores padrão ---
        binding.autoCompleteType.setText(pokemonTypesList[0].displayName, false)
        binding.autoCompleteGeneration.setText(geracoes[0], false)

        // --- 6. Adicionar listeners ---
        binding.autoCompleteType.setOnItemClickListener { parent, view, position, id ->
            val selectedType = pokemonTypesList[position]
            viewModel.filterPokemonByType(selectedType.apiKey)
        }

        binding.autoCompleteGeneration.setOnItemClickListener { parent, view, position, id ->
            val selectedGen = parent.getItemAtPosition(position) as String
            viewModel.filterPokemonByGeneration(selectedGen)
        }
    }
}