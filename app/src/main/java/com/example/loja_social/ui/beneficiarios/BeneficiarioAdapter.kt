package com.example.loja_social.ui.beneficiarios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.loja_social.R
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.databinding.ListItemBeneficiarioBinding

/**
 * Adapter para exibir beneficiários em um RecyclerView.
 * Mostra nome, contacto (email ou número de estudante) e estado (ativo/inativo).
 * Usa TextView estilizado como Chip para o estado (compatibilidade com minSdk 24).
 * 
 * @param onBeneficiarioClicked Callback chamado quando o utilizador clica em um beneficiário
 */
class BeneficiarioAdapter(private val onBeneficiarioClicked: (String) -> Unit) : ListAdapter<Beneficiario, BeneficiarioAdapter.BeneficiarioViewHolder>(BeneficiarioDiffCallback()) {

    inner class BeneficiarioViewHolder(private val binding: ListItemBeneficiarioBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Liga os dados do beneficiário aos componentes da UI.
         * Exibe nome, contacto e estado com cores dinâmicas.
         * @param beneficiario O beneficiário a exibir
         */
        fun bind(beneficiario: Beneficiario) {
            try {
                val context = binding.root.context

                // Exibe nome completo (ou "Sem nome" se null)
                binding.tvNomeCompleto.text = beneficiario.nomeCompleto ?: "Sem nome"

                // Exibe email ou número de estudante (prioridade para email)
                binding.tvDetalhe.text = beneficiario.email ?: beneficiario.numEstudante ?: "Sem contacto"

                // Configura estado (ativo/inativo) com TextView estilizado como Chip
                // Nota: Usa TextView em vez de Chip para compatibilidade com minSdk 24
                val estado = beneficiario.estado ?: "inativo"
                val isAtivo = estado.equals("ativo", ignoreCase = true)
                
                binding.tvEstado.text = if (isAtivo) "Ativo" else "Inativo"
                
                // Cria um GradientDrawable programaticamente para simular um Chip
                try {
                    val colorRes = if (isAtivo) R.color.estadoAtivo else R.color.estadoInativo
                    val backgroundColor = ContextCompat.getColor(context, colorRes)
                    
                    val drawable = android.graphics.drawable.GradientDrawable().apply {
                        setColor(backgroundColor)
                        cornerRadius = 12f * context.resources.displayMetrics.density // 12dp em pixels
                    }
                    binding.tvEstado.background = drawable
                    binding.tvEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                } catch (e: Exception) {
                    android.util.Log.e("BeneficiarioAdapter", "Erro ao configurar cor do estado", e)
                    // Fallback: usar cor de fundo diretamente
                    try {
                        val colorRes = if (isAtivo) R.color.estadoAtivo else R.color.estadoInativo
                        val backgroundColor = ContextCompat.getColor(context, colorRes)
                        binding.tvEstado.setBackgroundColor(backgroundColor)
                        binding.tvEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    } catch (e2: Exception) {
                        android.util.Log.e("BeneficiarioAdapter", "Erro crítico ao configurar estado", e2)
                    }
                }

                // Torna o item clicável e envia o ID do beneficiário
                binding.root.setOnClickListener {
                    try {
                        onBeneficiarioClicked(beneficiario.id)
                    } catch (e: Exception) {
                        android.util.Log.e("BeneficiarioAdapter", "Erro ao clicar no beneficiário", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BeneficiarioAdapter", "Erro ao fazer bind do beneficiário", e)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiarioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemBeneficiarioBinding.inflate(inflater, parent, false)
        return BeneficiarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BeneficiarioViewHolder, position: Int) {
        try {
            if (position >= 0 && position < itemCount) {
                holder.bind(getItem(position))
            }
        } catch (e: Exception) {
            android.util.Log.e("BeneficiarioAdapter", "Erro ao fazer bind na posição $position", e)
        }
    }
}

/**
 * Callback para DiffUtil usado pelo BeneficiarioAdapter.
 * Compara beneficiários pelo ID para determinar se são o mesmo item,
 * e compara todos os campos para determinar se o conteúdo mudou.
 */
class BeneficiarioDiffCallback : DiffUtil.ItemCallback<Beneficiario>() {
    /**
     * Verifica se dois itens representam o mesmo beneficiário (mesmo ID).
     */
    override fun areItemsTheSame(oldItem: Beneficiario, newItem: Beneficiario): Boolean {
        return try {
            oldItem.id == newItem.id
        } catch (e: Exception) {
            android.util.Log.e("BeneficiarioDiffCallback", "Erro ao comparar items", e)
            false
        }
    }

    /**
     * Verifica se o conteúdo de dois beneficiários é idêntico.
     */
    override fun areContentsTheSame(oldItem: Beneficiario, newItem: Beneficiario): Boolean {
        return try {
            oldItem == newItem
        } catch (e: Exception) {
            android.util.Log.e("BeneficiarioDiffCallback", "Erro ao comparar conteúdos", e)
            false
        }
    }
}
