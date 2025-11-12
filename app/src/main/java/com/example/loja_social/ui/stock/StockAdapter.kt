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

class StockAdapter(
    private val onStockItemClicked: (StockItem) -> Unit
) : ListAdapter<StockItem, StockAdapter.StockViewHolder>(StockDiffCallback()) {

    inner class StockViewHolder(private val binding: ListItemStockBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stockItem: StockItem) {
            val context = binding.root.context

            binding.tvProdutoNome.text = stockItem.produto
            binding.tvCategoria.text = stockItem.categoria ?: "Sem categoria"
            
            // Quantidade total
            binding.tvQuantidade.text = "${stockItem.quantidadeTotal} unidades"
            
            // Número de lotes
            binding.tvLotes.text = "${stockItem.lotes} ${if (stockItem.lotes == 1) "lote" else "lotes"}"

            // Data de validade mais próxima
            if (stockItem.validadeProxima != null) {
                try {
                    // Formatar data de yyyy-MM-dd para dd/MM/yyyy
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = inputFormat.parse(stockItem.validadeProxima)
                    if (date != null) {
                        val formattedDate = outputFormat.format(date)
                        binding.tvValidade.text = "Validade: $formattedDate"
                        binding.tvValidade.isVisible = true
                        
                        // Verificar se está próximo do vencimento
                        val daysUntilExpiry = ((date.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                        val drawable = android.graphics.drawable.GradientDrawable().apply {
                            cornerRadius = 12f * context.resources.displayMetrics.density
                        }
                        if (daysUntilExpiry < 0) {
                            // Já vencido
                            binding.chipValidade.text = "VENCIDO"
                            drawable.setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else if (daysUntilExpiry <= 7) {
                            // Próximo do vencimento (7 dias)
                            binding.chipValidade.text = "Vence em $daysUntilExpiry dias"
                            drawable.setColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else if (daysUntilExpiry <= 30) {
                            // Próximo do vencimento (30 dias)
                            binding.chipValidade.text = "Vence em $daysUntilExpiry dias"
                            drawable.setColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else {
                            binding.chipValidade.isVisible = false
                        }
                    } else {
                        binding.tvValidade.isVisible = false
                        binding.chipValidade.isVisible = false
                    }
                } catch (e: Exception) {
                    binding.tvValidade.text = "Validade: ${stockItem.validadeProxima}"
                    binding.tvValidade.isVisible = true
                    binding.chipValidade.isVisible = false
                }
            } else {
                binding.tvValidade.isVisible = false
                binding.chipValidade.isVisible = false
            }

            // Torna o item clicável
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

class StockDiffCallback : DiffUtil.ItemCallback<StockItem>() {
    override fun areItemsTheSame(oldItem: StockItem, newItem: StockItem): Boolean {
        return oldItem.produtoId == newItem.produtoId
    }

    override fun areContentsTheSame(oldItem: StockItem, newItem: StockItem): Boolean {
        return oldItem == newItem
    }
}

