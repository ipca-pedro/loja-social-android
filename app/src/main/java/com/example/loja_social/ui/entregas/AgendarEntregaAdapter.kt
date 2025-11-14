package com.example.loja_social.ui.entregas

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loja_social.databinding.ListItemAgendarEntregaBinding

class AgendarEntregaAdapter(
    private val onRemoveClicked: (String) -> Unit,
    private val onQuantityChanged: (String, Int) -> Unit
) : ListAdapter<ItemSelecionado, AgendarEntregaAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListItemAgendarEntregaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: ListItemAgendarEntregaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var textWatcher: TextWatcher? = null

        fun bind(item: ItemSelecionado) {
            binding.tvProdutoNome.text = item.lote.produto
            binding.tvLoteInfo.text = "Lote: ...${item.lote.id.takeLast(4)} | Val: ${item.lote.dataValidade ?: "N/A"}"

            // Remove o listener antigo para evitar loops infinitos
            binding.etQuantidade.removeTextChangedListener(textWatcher)
            binding.etQuantidade.setText(item.quantidade.toString())

            // Cria e adiciona um novo listener
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val novaQuantidade = s?.toString()?.toIntOrNull() ?: 0
                    if (novaQuantidade != item.quantidade) {
                        onQuantityChanged(item.lote.id, novaQuantidade)
                    }
                }
            }
            binding.etQuantidade.addTextChangedListener(textWatcher)

            binding.btnRemoverItem.setOnClickListener {
                onRemoveClicked(item.lote.id)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemSelecionado>() {
        override fun areItemsTheSame(oldItem: ItemSelecionado, newItem: ItemSelecionado): Boolean {
            return oldItem.lote.id == newItem.lote.id
        }

        override fun areContentsTheSame(oldItem: ItemSelecionado, newItem: ItemSelecionado): Boolean {
            return oldItem == newItem
        }
    }
}