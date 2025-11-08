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
import com.example.projetoventurus.R // ◀️ IMPORTANTE: Importe o R do seu app
import com.example.projetoventurus.adapter.PokemonAdapter
import com.example.projetoventurus.databinding.ActivityMainBinding
import com.example.projetoventurus.viewmodel.PokemonViewModel

// ◀️ ADICIONADO: Data class para ligar o nome de exibição à chave da API
data class PokemonType(val displayName: String, val apiKey: String)

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

    // ◀️ ATUALIZADA: Função de filtros foi reescrita
    private fun setupFilters() {

        // --- 1. Criar a lista de tipos usando as traduções ---

        // Lista das chaves de API (que correspondem ao final das nossas chaves do strings.xml)
        val apiTypeKeys = listOf(
            "all", "fire", "water", "grass", "electric", "bug", "poison",
            "normal", "flying", "ground", "fairy", "fighting", "psychic",
            "rock", "ghost", "ice", "dragon"
        )

        // Cria a lista de objetos PokemonType
        val pokemonTypesList = apiTypeKeys.map { key ->
            // Encontra o ID do recurso string (ex: R.string.type_fire)
            val resId = resources.getIdentifier("type_$key", "string", packageName)

            // Busca a string traduzida (ex: "Fogo")
            val translatedName = getString(resId)

            PokemonType(displayName = translatedName, apiKey = key)
        }

        // --- 2. Criar a lista de gerações (pode ser traduzida da mesma forma se quiser) ---
        val geracoes = listOf(
            "Geração I", "Geração II", "Geração III", "Geração IV", "Geração V"
        )

        // --- 3. Criar os Adapters ---

        // O adapter de tipo agora usa apenas os nomes de exibição (traduzidos)
        val typeDisplayNames = pokemonTypesList.map { it.displayName }
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, typeDisplayNames)

        val generationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, geracoes)

        // --- 4. Conectar os Adapters ---
        binding.autoCompleteType.setAdapter(typeAdapter)
        binding.autoCompleteGeneration.setAdapter(generationAdapter)

        // --- 5. Definir valores padrão ---
        binding.autoCompleteType.setText(pokemonTypesList[0].displayName, false) // Mostra "Todos os Tipos"
        binding.autoCompleteGeneration.setText(geracoes[0], false)

        // --- 6. Adicionar listeners ---
        binding.autoCompleteType.setOnItemClickListener { parent, view, position, id ->
            // Encontra o objeto PokemonType que foi clicado
            val selectedType = pokemonTypesList[position]

            // ◀️ Envia a CHAVE DA API ("fire", "water", "all") para o ViewModel
            viewModel.filterPokemonByType(selectedType.apiKey)
        }

        binding.autoCompleteGeneration.setOnItemClickListener { parent, view, position, id ->
            val selectedGen = parent.getItemAtPosition(position) as String
            viewModel.filterPokemonByGeneration(selectedGen)
        }
    }
}