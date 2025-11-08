package com.example.projetoventurus.ui // ◀️ CORRIGIDO

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
// Imports corrigidos
import com.example.projetoventurus.databinding.ActivityDetailBinding // ◀️ CORRIGIDO
import com.example.projetoventurus.network.PokemonDetailResponse // ◀️ CORRIGIDO
import com.example.projetoventurus.viewmodel.PokemonViewModel // ◀️ CORRIGIDO

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: PokemonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Habilita o botão "voltar" na barra de ação
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Pega o nome do Pokémon passado pela Intent
        val pokemonName = intent.getStringExtra("POKEMON_NAME")

        if (pokemonName == null) {
            Toast.makeText(this, "Erro: Nome do Pokémon não encontrado.", Toast.LENGTH_LONG).show()
            finish() // Fecha a activity se não houver nome
            return
        }

        setupObservers()
        viewModel.fetchPokemonDetail(pokemonName)
    }

    private fun setupObservers() {
        viewModel.pokemonDetail.observe(this) { detail ->
            bindPokemonDetails(detail)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentScrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bindPokemonDetails(detail: PokemonDetailResponse) {
        // Nome e ID
        binding.tvDetailName.text = detail.name.replaceFirstChar { it.uppercase() }
        binding.tvDetailId.text = "#${detail.id.toString().padStart(3, '0')}"

        // Imagem (usando Glide)
        Glide.with(this)
            .load(detail.sprites.other.officialArtwork.frontDefault)
            .into(binding.ivDetailImage)

        // Tipos (junta os nomes dos tipos)
        val types = detail.types.joinToString(", ") { it.type.name.uppercase() }
        binding.tvDetailTypes.text = "Tipo: $types"

        // Altura e Peso (convertendo de decímetros/hectogramas)
        val heightInMeters = detail.height / 10.0
        val weightInKg = detail.weight / 10.0
        binding.tvDetailHeight.text = "Altura: ${heightInMeters} m"
        binding.tvDetailWeight.text = "Peso: ${weightInKg} kg"

        // Stats (HP, Ataque, Defesa)
        binding.tvStatHp.text = "HP: ${findStat(detail, "hp")}"
        binding.tvStatAttack.text = "Ataque: ${findStat(detail, "attack")}"
        binding.tvStatDefense.text = "Defesa: ${findStat(detail, "defense")}"
    }

    // Função auxiliar para encontrar um stat específico na lista
    private fun findStat(detail: PokemonDetailResponse, statName: String): Int {
        return detail.stats.find { it.stat.name == statName }?.baseStat ?: 0
    }

    // Lida com o clique no botão "voltar" da barra de ação
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}