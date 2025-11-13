package com.example.loja_social.ui.stock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loja_social.R
import com.example.loja_social.api.StockItem
import com.example.loja_social.databinding.ListItemStockBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para exibir itens de stock agrupados por produto em um RecyclerView.
 * Mostra informações do produto, quantidade total, número de lotes e alertas de validade.
 * 
 * @param onStockItemClicked Callback chamado quando o utilizador clica em um item de stock
 */
class StockAdapter(
    private val onStockItemClicked: (StockItem) -> Unit
) : ListAdapter<StockItem, StockAdapter.StockViewHolder>(StockDiffCallback()) {

    inner class StockViewHolder(private val binding: ListItemStockBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Liga os dados do item de stock aos componentes da UI.
         * Formata datas, calcula alertas de validade e configura clique.
         * @param stockItem O item de stock a exibir
         */
        fun bind(stockItem: StockItem) {
            val context = binding.root.context

            binding.tvProdutoNome.text = stockItem.produto
            binding.tvCategoria.text = stockItem.categoria ?: "Sem categoria"
            
            // Exibe quantidade total e número de lotes
            binding.tvQuantidade.text = "${stockItem.quantidadeTotal} unidades"
            binding.tvLotes.text = "${stockItem.lotes} ${if (stockItem.lotes == 1) "lote" else "lotes"}"

            // Formata e exibe data de validade mais próxima (se existir)
            if (stockItem.validadeProxima != null) {
                try {
                    // Converte de yyyy-MM-dd para dd/MM/yyyy
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = inputFormat.parse(stockItem.validadeProxima)
                    if (date != null) {
                        val formattedDate = outputFormat.format(date)
                        binding.tvValidade.text = "Validade: $formattedDate"
                        binding.tvValidade.isVisible = true
                        
                        // Calcula dias até o vencimento e exibe alertas coloridos
                        val daysUntilExpiry = ((date.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                        val drawable = android.graphics.drawable.GradientDrawable().apply {
                            cornerRadius = 12f * context.resources.displayMetrics.density
                        }
                        if (daysUntilExpiry < 0) {
                            // Vencido: vermelho
                            binding.chipValidade.text = "VENCIDO"
                            drawable.setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else if (daysUntilExpiry <= 7) {
                            // Vence em 7 dias ou menos: laranja escuro
                            binding.chipValidade.text = "Vence em $daysUntilExpiry dias"
                            drawable.setColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else if (daysUntilExpiry <= 30) {
                            // Vence em 30 dias ou menos: laranja claro
                            binding.chipValidade.text = "Vence em $daysUntilExpiry dias"
                            drawable.setColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else {
                            // Mais de 30 dias: não mostra alerta
                            binding.chipValidade.isVisible = false
                        }
                    } else {
                        binding.tvValidade.isVisible = false
                        binding.chipValidade.isVisible = false
                    }
                } catch (e: Exception) {
                    // Em caso de erro, mostra a data original
                    binding.tvValidade.text = "Validade: ${stockItem.validadeProxima}"
                    binding.tvValidade.isVisible = true
                    binding.chipValidade.isVisible = false
                }
            } else {
                // Sem data de validade: oculta os campos relacionados
                binding.tvValidade.isVisible = false
                binding.chipValidade.isVisible = false
            }

            // Torna o item clicável para navegar para detalhes
            binding.root.setOnClickListener {
                onStockItemClicked(stockItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemStockBinding.inflate(inflater, parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * Callback para DiffUtil usado pelo StockAdapter.
 * Compara itens de stock pelo produtoId para determinar se são o mesmo item,
 * e compara todos os campos para determinar se o conteúdo mudou.
 */
class StockDiffCallback : DiffUtil.ItemCallback<StockItem>() {
    /**
     * Verifica se dois itens representam o mesmo produto (mesmo produtoId).
     */
    override fun areItemsTheSame(oldItem: StockItem, newItem: StockItem): Boolean {
        return oldItem.produtoId == newItem.produtoId
    }

    /**
     * Verifica se o conteúdo de dois itens de stock é idêntico.
     */
    override fun areContentsTheSame(oldItem: StockItem, newItem: StockItem): Boolean {
        return oldItem == newItem
    }
}

