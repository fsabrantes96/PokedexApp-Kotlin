package com.example.projetoventurus.ui

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.projetoventurus.databinding.ActivityDetailBinding
import com.example.projetoventurus.network.PokemonDetailResponse
import com.example.projetoventurus.viewmodel.PokemonViewModel

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: PokemonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val pokemonName = intent.getStringExtra("POKEMON_NAME")

        if (pokemonName == null) {
            Toast.makeText(this, "Erro: Nome do Pokémon não encontrado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupObservers()
        viewModel.fetchPokemonDetail(pokemonName)
    }

    private fun setupObservers() {
        viewModel.pokemonDetail.observe(this) { detail ->
            bindPokemonDetails(detail)
        }

        // As chamadas aqui estão corretas (isLoading / error)
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

        // Imagem (com Listener do Palette)
        Glide.with(this)
            .load(detail.sprites.other.officialArtwork.frontDefault)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    applyDynamicColors(resource)
                    return false
                }
            })
            .into(binding.ivDetailImage)

        // Tipos
        val types = detail.types.joinToString(", ") { it.type.name.uppercase() }
        binding.tvDetailTypes.text = "Tipo: $types"

        // Altura e Peso
        val heightInMeters = detail.height / 10.0
        val weightInKg = detail.weight / 10.0
        binding.tvDetailHeight.text = "Altura: ${heightInMeters} m"
        binding.tvDetailWeight.text = "Peso: ${weightInKg} kg"

        // Stats
        binding.tvStatHp.text = "HP: ${findStat(detail, "hp")}"
        binding.tvStatAttack.text = "Ataque: ${findStat(detail, "attack")}"
        binding.tvStatDefense.text = "Defesa: ${findStat(detail, "defense")}"

        // Lógica de tocar o som
        val cryUrl = detail.cries.latest
        binding.btnPlayCry.setOnClickListener {
            playPokemonCry(cryUrl)
        }
    }

    private fun playPokemonCry(url: String) {
        binding.btnPlayCry.isEnabled = false

        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            prepareAsync()

            setOnPreparedListener {
                it.start()
            }
            setOnCompletionListener {
                it.release()
                binding.btnPlayCry.isEnabled = true
            }
            setOnErrorListener { mp, what, extra ->
                Toast.makeText(this@DetailActivity, "Erro ao tocar som.", Toast.LENGTH_SHORT).show()
                mp.release()
                binding.btnPlayCry.isEnabled = true
                true
            }
        }
    }

    private fun applyDynamicColors(drawable: Drawable) {
        val bitmap = (drawable as BitmapDrawable).bitmap

        Palette.from(bitmap).generate { palette ->
            val swatch = palette?.vibrantSwatch ?: palette?.dominantSwatch ?: return@generate

            val backgroundColor = swatch.rgb
            window.statusBarColor = backgroundColor
            supportActionBar?.setBackgroundDrawable(ColorDrawable(backgroundColor))
        }
    }

    private fun findStat(detail: PokemonDetailResponse, statName: String): Int {
        return detail.stats.find { it.stat.name == statName }?.baseStat ?: 0
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFinishAfterTransition()
        return true
    }
}