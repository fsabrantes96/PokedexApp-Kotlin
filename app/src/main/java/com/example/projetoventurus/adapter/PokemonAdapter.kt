package com.example.projetoventurus.adapter

import android.view.LayoutInflater
import android.view.View // ◀️ Importe a View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projetoventurus.R
import com.example.projetoventurus.databinding.ItemPokemonBinding
import com.example.projetoventurus.network.PokemonResult

class PokemonAdapter(
    // ◀️ ATUALIZADO: O lambda agora passa o Pokémon E a View (ImageView) que foi clicada
    private val onItemClicked: (PokemonResult, View) -> Unit
) : ListAdapter<PokemonResult, PokemonAdapter.PokemonViewHolder>(PokemonDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val binding = ItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PokemonViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = getItem(position)
        holder.bind(pokemon)
    }

    // --- ViewHolder ---
    class PokemonViewHolder(
        private val binding: ItemPokemonBinding,
        private val onItemClicked: (PokemonResult, View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pokemon: PokemonResult) {
            // Define o nome
            binding.tvPokemonName.text = pokemon.name.replaceFirstChar { it.uppercase() }

            // Define o ID
            binding.tvPokemonId.text = "#${pokemon.getId().toString().padStart(3, '0')}"

            // Carrega a imagem usando Glide
            Glide.with(binding.root.context)
                .load(pokemon.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.ivPokemonImage)

            // ◀️ ATUALIZADO: Define o listener de clique na IMAGEM, não no 'root'
            binding.ivPokemonImage.setOnClickListener {
                // Passa o pokémon e a própria ImageView para o lambda
                onItemClicked(pokemon, binding.ivPokemonImage)
            }
        }
    }

    // --- DiffUtil Callback ---
    object PokemonDiffCallback : DiffUtil.ItemCallback<PokemonResult>() {
        override fun areItemsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean {
            return oldItem == newItem
        }
    }
}