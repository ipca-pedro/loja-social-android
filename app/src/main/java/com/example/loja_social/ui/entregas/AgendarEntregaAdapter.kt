package com.example.loja_social.ui.entregas

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loja_social.R
import com.example.loja_social.databinding.ListItemAgendarEntregaBinding

/**
 * Adapter para exibir a lista de itens selecionados para uma entrega.
 * Permite editar a quantidade de cada item e remover itens da lista.
 * 
 * @param onRemoveClicked Callback chamado quando um item é removido (recebe o ID do lote)
 * @param onQuantityChanged Callback chamado quando a quantidade de um item é alterada (recebe ID do lote e nova quantidade)
 */
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

    /**
     * ViewHolder para um item da lista de itens selecionados.
     */
    inner class ItemViewHolder(private val binding: ListItemAgendarEntregaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var textWatcher: TextWatcher? = null

        /**
         * Liga os dados do item ao ViewHolder.
         * Configura o TextWatcher para atualizar a quantidade em tempo real.
         * Aplica validação visual quando a quantidade excede o disponível.
         * 
         * @param item O item selecionado a exibir
         */
        fun bind(item: ItemSelecionado) {
            binding.tvProdutoNome.text = item.lote.produto
            binding.tvLoteInfo.text = "Lote: ...${item.lote.id.takeLast(4)} | Val: ${item.lote.dataValidade ?: "N/A"}"

            // Remove o listener antigo para evitar loops infinitos
            binding.etQuantidade.removeTextChangedListener(textWatcher)
            binding.etQuantidade.setText(item.quantidade.toString())

            // Validação visual: cor vermelha se quantidade exceder o disponível
            val isQuantidadeValida = item.quantidade > 0 && item.quantidade <= item.lote.quantidadeAtual
            val textColor = if (isQuantidadeValida) {
                ContextCompat.getColor(binding.root.context, android.R.color.black)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.estadoInativo)
            }
            binding.etQuantidade.setTextColor(textColor)

            // Cria e adiciona um novo listener para atualizar quantidade em tempo real
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val novaQuantidade = s?.toString()?.toIntOrNull() ?: 0
                    // Validação visual em tempo real
                    val isValid = novaQuantidade > 0 && novaQuantidade <= item.lote.quantidadeAtual
                    val color = if (isValid) {
                        ContextCompat.getColor(binding.root.context, android.R.color.black)
                    } else {
                        ContextCompat.getColor(binding.root.context, R.color.estadoInativo)
                    }
                    binding.etQuantidade.setTextColor(color)
                    
                    // Notifica o ViewModel apenas se a quantidade mudou
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

    /**
     * Callback do DiffUtil para comparar itens e otimizar atualizações da lista.
     */
    class DiffCallback : DiffUtil.ItemCallback<ItemSelecionado>() {
        override fun areItemsTheSame(oldItem: ItemSelecionado, newItem: ItemSelecionado): Boolean {
            return oldItem.lote.id == newItem.lote.id
        }

        override fun areContentsTheSame(oldItem: ItemSelecionado, newItem: ItemSelecionado): Boolean {
            return oldItem == newItem
        }
    }
}