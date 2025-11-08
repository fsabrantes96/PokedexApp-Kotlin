package com.example.projetoventurus.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projetoventurus.adapter.PokemonAdapter
import com.example.projetoventurus.databinding.ActivityMainBinding
import com.example.projetoventurus.viewmodel.PokemonViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: PokemonViewModel by viewModels()
    private lateinit var pokemonAdapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupSearchView()
        setupFilters() // Configuração inicial dos spinners

        // Busca os dados na primeira vez que a Activity é criada
        if (savedInstanceState == null) {
            viewModel.fetchPokemonList()
        }
    }

    private fun setupRecyclerView() {
        pokemonAdapter = PokemonAdapter { pokemon ->
            // Ação de clique
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("POKEMON_NAME", pokemon.name)
            startActivity(intent)
        }

        binding.rvPokemonList.apply {
            adapter = pokemonAdapter
            // Exibe em grade de 2 colunas
            layoutManager = GridLayoutManager(this@MainActivity, 2)
        }
    }

    private fun setupObservers() {
        // Observa a lista filtrada
        viewModel.pokemonListFiltred.observe(this) { list ->
            pokemonAdapter.submitList(list)
            // Mostra mensagem se a lista estiver vazia após filtro
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
            // Chamado quando o usuário aperta "Enter"
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterPokemonByName(query.orEmpty())
                return true
            }

            // Chamado a cada caractere digitado
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterPokemonByName(newText.orEmpty())
                return true
            }
        })
    }
    private fun setupFilters() {
        // --- 1. Criar os dados para os filtros ---
        val tipos = listOf(
            "Todos os Tipos", "Fire", "Water", "Grass", "Electric",
            "Bug", "Poison", "Normal", "Flying", "Ground", "Fairy",
            "Fighting", "Psychic", "Rock", "Ghost", "Ice", "Dragon"
        )

        // ◀️ ATUALIZADO: Lista de gerações
        val geracoes = listOf(
            "Geração I",
            "Geração II",
            "Geração III",
            "Geração IV",
            "Geração V"
        )

        // --- 2. Criar os Adapters ---
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos)
        val generationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, geracoes)

        // --- 3. Conectar os Adapters aos AutoCompleteTextViews ---
        binding.autoCompleteType.setAdapter(typeAdapter)
        binding.autoCompleteGeneration.setAdapter(generationAdapter)

        // --- 4. (Opcional) Definir um valor padrão ---
        binding.autoCompleteType.setText(tipos[0], false)
        binding.autoCompleteGeneration.setText(geracoes[0], false)

        // --- 5. Adicionar listeners para quando um item for selecionado ---
        binding.autoCompleteType.setOnItemClickListener { parent, view, position, id ->
            val selectedType = parent.getItemAtPosition(position) as String

            // Chama o ViewModel para filtrar por TIPO
            viewModel.filterPokemonByType(selectedType)
        }

        binding.autoCompleteGeneration.setOnItemClickListener { parent, view, position, id ->
            val selectedGen = parent.getItemAtPosition(position) as String

            // ◀️ ATUALIZADO: Chama o ViewModel para carregar a GERAÇÃO
            viewModel.filterPokemonByGeneration(selectedGen)
        }
    }
}