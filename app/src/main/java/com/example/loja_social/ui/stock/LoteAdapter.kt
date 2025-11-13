package com.example.loja_social.ui.stock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loja_social.R
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.databinding.ListItemLoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoteAdapter(
    private val onEditClick: (LoteIndividual) -> Unit,
    private val onDeleteClick: (LoteIndividual) -> Unit
) : ListAdapter<LoteIndividual, LoteAdapter.LoteViewHolder>(LoteDiffCallback()) {

    inner class LoteViewHolder(private val binding: ListItemLoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lote: LoteIndividual) {
            val context = binding.root.context

            // Quantidade
            binding.tvQuantidade.text = "${lote.quantidadeAtual} / ${lote.quantidadeInicial}"
            
            // Data de entrada
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(lote.dataEntrada)
                if (date != null) {
                    binding.tvDataEntrada.text = "Entrada: ${outputFormat.format(date)}"
                } else {
                    binding.tvDataEntrada.text = "Entrada: ${lote.dataEntrada}"
                }
            } catch (e: Exception) {
                binding.tvDataEntrada.text = "Entrada: ${lote.dataEntrada}"
            }

            // Data de validade
            if (lote.dataValidade != null) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = inputFormat.parse(lote.dataValidade)
                    if (date != null) {
                        val formattedDate = outputFormat.format(date)
                        binding.tvDataValidade.text = "Validade: $formattedDate"
                        binding.tvDataValidade.isVisible = true
                        
                        // Verificar se está próximo do vencimento
                        val daysUntilExpiry = ((date.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                        if (daysUntilExpiry < 0) {
                            binding.chipValidade.text = "VENCIDO"
                            val drawable = android.graphics.drawable.GradientDrawable().apply {
                                cornerRadius = 12f * context.resources.displayMetrics.density
                                setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                            }
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else if (daysUntilExpiry <= 7) {
                            binding.chipValidade.text = "Vence em $daysUntilExpiry dias"
                            val drawable = android.graphics.drawable.GradientDrawable().apply {
                                cornerRadius = 12f * context.resources.displayMetrics.density
                                setColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                            }
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else if (daysUntilExpiry <= 30) {
                            binding.chipValidade.text = "Vence em $daysUntilExpiry dias"
                            val drawable = android.graphics.drawable.GradientDrawable().apply {
                                cornerRadius = 12f * context.resources.displayMetrics.density
                                setColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                            }
                            binding.chipValidade.background = drawable
                            binding.chipValidade.isVisible = true
                        } else {
                            binding.chipValidade.isVisible = false
                        }
                    }
                } catch (e: Exception) {
                    binding.tvDataValidade.text = "Validade: ${lote.dataValidade}"
                    binding.tvDataValidade.isVisible = true
                    binding.chipValidade.isVisible = false
                }
            } else {
                binding.tvDataValidade.isVisible = false
                binding.chipValidade.isVisible = false
            }

            // ID do lote (truncado)
            val loteIdShort = if (lote.id.length >= 8) lote.id.substring(0, 8) else lote.id
            binding.tvLoteId.text = "Lote: $loteIdShort..."

            // Botões
            binding.btnEdit.setOnClickListener { onEditClick(lote) }
            binding.btnDelete.setOnClickListener { onDeleteClick(lote) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemLoteBinding.inflate(inflater, parent, false)
        return LoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LoteDiffCallback : DiffUtil.ItemCallback<LoteIndividual>() {
    override fun areItemsTheSame(oldItem: LoteIndividual, newItem: LoteIndividual): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LoteIndividual, newItem: LoteIndividual): Boolean {
        return oldItem == newItem
    }
}

