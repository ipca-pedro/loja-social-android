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

/**
 * Adapter para exibir lotes individuais de stock em um RecyclerView.
 * Mostra informações do lote, alertas de validade e botões de ação (editar/remover).
 * 
 * @param onEditClick Callback chamado quando o utilizador clica em "Editar"
 * @param onDeleteClick Callback chamado quando o utilizador clica em "Remover"
 */
class LoteAdapter(
    private val onEditClick: (LoteIndividual) -> Unit,
    private val onDeleteClick: (LoteIndividual) -> Unit
) : ListAdapter<LoteIndividual, LoteAdapter.LoteViewHolder>(LoteDiffCallback()) {

    inner class LoteViewHolder(private val binding: ListItemLoteBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Liga os dados do lote aos componentes da UI.
         * Formata datas, calcula alertas de validade e configura botões de ação.
         * @param lote O lote a exibir
         */
        fun bind(lote: LoteIndividual) {
            val context = binding.root.context

            // Exibe quantidade atual vs inicial (ex: "50 / 100")
            binding.tvQuantidade.text = "${lote.quantidadeAtual} / ${lote.quantidadeInicial}"
            
            // Formata data de entrada (converte de yyyy-MM-dd para dd/MM/yyyy)
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

            // Formata e exibe data de validade (se existir)
            if (lote.dataValidade != null) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = inputFormat.parse(lote.dataValidade)
                    if (date != null) {
                        val formattedDate = outputFormat.format(date)
                        binding.tvDataValidade.text = "Validade: $formattedDate"
                        binding.tvDataValidade.isVisible = true
                        
                        // Calcula dias até o vencimento e exibe alertas coloridos
                        val daysUntilExpiry = ((date.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                        if (daysUntilExpiry < 0) {
                            // Vencido: vermelho
                            binding.tvChipValidade.text = "VENCIDO"
                            binding.chipValidade.setCardBackgroundColor(
                                ContextCompat.getColor(context, android.R.color.holo_red_dark)
                            )
                            binding.chipValidade.isVisible = true
                        } else if (daysUntilExpiry <= 7) {
                            // Vence em 7 dias ou menos: laranja escuro
                            binding.tvChipValidade.text = "Vence em $daysUntilExpiry dias"
                            binding.chipValidade.setCardBackgroundColor(
                                ContextCompat.getColor(context, android.R.color.holo_orange_dark)
                            )
                            binding.chipValidade.isVisible = true
                        } else if (daysUntilExpiry <= 30) {
                            // Vence em 30 dias ou menos: laranja claro
                            binding.tvChipValidade.text = "Vence em $daysUntilExpiry dias"
                            binding.chipValidade.setCardBackgroundColor(
                                ContextCompat.getColor(context, android.R.color.holo_orange_light)
                            )
                            binding.chipValidade.isVisible = true
                        } else {
                            // Mais de 30 dias: não mostra alerta
                            binding.chipValidade.isVisible = false
                        }
                    }
                } catch (e: Exception) {
                    // Em caso de erro no parsing, mostra a data original
                    binding.tvDataValidade.text = "Validade: ${lote.dataValidade}"
                    binding.tvDataValidade.isVisible = true
                    binding.chipValidade.isVisible = false
                }
            } else {
                // Sem data de validade: oculta os campos relacionados
                binding.tvDataValidade.isVisible = false
                binding.chipValidade.isVisible = false
            }

            // Exibe ID do lote truncado (primeiros 8 caracteres)
            val loteIdShort = if (lote.id.length >= 8) lote.id.substring(0, 8) else lote.id
            binding.tvLoteId.text = "Lote: $loteIdShort..."

            // Configura botões de ação
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

/**
 * Callback para DiffUtil usado pelo LoteAdapter.
 * Compara lotes pelo ID para determinar se são o mesmo item,
 * e compara todos os campos para determinar se o conteúdo mudou.
 */
class LoteDiffCallback : DiffUtil.ItemCallback<LoteIndividual>() {
    /**
     * Verifica se dois itens representam o mesmo lote (mesmo ID).
     */
    override fun areItemsTheSame(oldItem: LoteIndividual, newItem: LoteIndividual): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Verifica se o conteúdo de dois lotes é idêntico.
     */
    override fun areContentsTheSame(oldItem: LoteIndividual, newItem: LoteIndividual): Boolean {
        return oldItem == newItem
    }
}

