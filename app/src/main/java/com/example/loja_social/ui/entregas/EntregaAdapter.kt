package com.example.loja_social.ui.entregas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loja_social.R // IMPORT OBRIGATÓRIO
import com.example.loja_social.api.Entrega
import com.example.loja_social.databinding.ListItemEntregaBinding
import java.util.Locale

/**
 * Adapter para exibir entregas em um RecyclerView.
 * Mostra informações do beneficiário, data de agendamento e estado.
 * Para entregas agendadas, exibe botão "Concluir"; para entregas concluídas, exibe estado.
 * 
 * Nota: Usa manipulação de string em vez de java.time para compatibilidade com minSdk 24.
 * 
 * @param onConcluirClicked Callback chamado quando o utilizador clica em "Concluir" uma entrega agendada
 */
class EntregaAdapter(private val onConcluirClicked: (Entrega) -> Unit) : ListAdapter<Entrega, EntregaAdapter.EntregaViewHolder>(EntregaDiffCallback()) {

    inner class EntregaViewHolder(private val binding: ListItemEntregaBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Liga os dados da entrega aos componentes da UI.
         * Exibe beneficiário, data, colaborador e estado/botão de ação.
         * @param entrega A entrega a exibir
         */
        fun bind(entrega: Entrega) {
            val context = binding.root.context

            // Exibe nome do beneficiário e número de estudante
            binding.tvBeneficiarioNome.text = "${entrega.beneficiario} (${entrega.numEstudante ?: "N/A"})"
            // Formata data e exibe colaborador
            binding.tvDataAgendamento.text = formatarData(entrega.dataAgendamento) + " | Colab: ${entrega.colaborador}"

            val isAgendada = entrega.estado.equals("agendada", ignoreCase = true)

            // Mostra botão "Concluir" apenas para entregas agendadas
            // Mostra estado (chip) apenas para entregas concluídas
            binding.btnConcluir.visibility = if (isAgendada) View.VISIBLE else View.GONE
            binding.tvEstadoConcluido.visibility = if (!isAgendada) View.VISIBLE else View.GONE

            if (!isAgendada) {
                // Entrega concluída: exibe estado com chip colorido
                val estado = entrega.estado.uppercase(Locale.getDefault())
                binding.tvEstadoConcluido.text = estado
                val corRes = when (estado) {
                    "ENTREGUE" -> R.color.estadoAtivo // Verde para entregue
                    else -> android.R.color.holo_red_dark // Vermelho para outros estados
                }
                binding.tvEstadoConcluido.setChipBackgroundColorResource(corRes)
                binding.tvEstadoConcluido.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                // Entrega agendada: configura botão de concluir
                binding.btnConcluir.setOnClickListener {
                    onConcluirClicked(entrega)
                }
            }
        }

        /**
         * Formata uma data ISO string para formato DD/MM/YYYY.
         * Usa manipulação de string em vez de java.time para compatibilidade com minSdk 24.
         * 
         * @param isoString Data no formato ISO (YYYY-MM-DDTHH:MM:SS.sssZ ou YYYY-MM-DD)
         * @return Data formatada como DD/MM/YYYY
         */
        private fun formatarData(isoString: String): String {
            return try {
                // Pega apenas a parte da data (YYYY-MM-DD)
                val datePart = isoString.substring(0, 10)
                val parts = datePart.split("-") // [YYYY, MM, DD]

                if (parts.size == 3) {
                    // Reorganiza para DD/MM/YYYY
                    "${parts[2]}/${parts[1]}/${parts[0]}"
                } else {
                    datePart // Fallback
                }
            } catch (e: Exception) {
                isoString.substring(0, 10) // Fallback seguro em caso de string inválida
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntregaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemEntregaBinding.inflate(inflater, parent, false)
        return EntregaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntregaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * Callback para DiffUtil usado pelo EntregaAdapter.
 * Compara entregas pelo ID para determinar se são o mesmo item,
 * e compara todos os campos para determinar se o conteúdo mudou.
 */
class EntregaDiffCallback : DiffUtil.ItemCallback<Entrega>() {
    /**
     * Verifica se dois itens representam a mesma entrega (mesmo ID).
     */
    override fun areItemsTheSame(oldItem: Entrega, newItem: Entrega): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Verifica se o conteúdo de duas entregas é idêntico.
     */
    override fun areContentsTheSame(oldItem: Entrega, newItem: Entrega): Boolean {
        return oldItem == newItem
    }
}