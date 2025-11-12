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

// NOTA: Removemos os imports de java.time para evitar falhas de execução no minSdk 24

class EntregaAdapter(private val onConcluirClicked: (String) -> Unit) : ListAdapter<Entrega, EntregaAdapter.EntregaViewHolder>(EntregaDiffCallback()) {

    inner class EntregaViewHolder(private val binding: ListItemEntregaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entrega: Entrega) {
            val context = binding.root.context

            binding.tvBeneficiarioNome.text = "${entrega.beneficiario} (${entrega.numEstudante ?: "N/A"})"
            binding.tvDataAgendamento.text = formatarData(entrega.dataAgendamento) + " | Colab: ${entrega.colaborador}"

            val isAgendada = entrega.estado.equals("agendada", ignoreCase = true)

            // Lógica do Botão/Estado
            binding.btnConcluir.visibility = if (isAgendada) View.VISIBLE else View.GONE
            binding.tvEstadoConcluido.visibility = if (!isAgendada) View.VISIBLE else View.GONE

            if (!isAgendada) {
                // Se não está agendada, mostra o estado real (Entregue / Não_Entregue) usando Chip
                val estado = entrega.estado.uppercase(Locale.getDefault())
                binding.tvEstadoConcluido.text = estado
                val corRes = when (estado) {
                    "ENTREGUE" -> R.color.estadoAtivo
                    else -> android.R.color.holo_red_dark // Outros estados (e.g. NAO_ENTREGUE)
                }
                binding.tvEstadoConcluido.setChipBackgroundColorResource(corRes)
                binding.tvEstadoConcluido.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            } else {
                // Ação do botão
                binding.btnConcluir.setOnClickListener {
                    onConcluirClicked(entrega.id)
                }
            }
        }

        /**
         * CORREÇÃO: Usa manipulação de string em vez de java.time para compatibilidade com minSdk 24.
         * Formato de entrada: YYYY-MM-DDTHH:MM:SS.sssZ
         * Formato de saída: DD/MM/YYYY
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

class EntregaDiffCallback : DiffUtil.ItemCallback<Entrega>() {
    override fun areItemsTheSame(oldItem: Entrega, newItem: Entrega): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Entrega, newItem: Entrega): Boolean {
        return oldItem == newItem
    }
}