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

class BeneficiarioAdapter(private val onBeneficiarioClicked: (String) -> Unit) : ListAdapter<Beneficiario, BeneficiarioAdapter.BeneficiarioViewHolder>(BeneficiarioDiffCallback()) {

    inner class BeneficiarioViewHolder(private val binding: ListItemBeneficiarioBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(beneficiario: Beneficiario) {
            try {
                val context = binding.root.context

                // Preencher nome
                binding.tvNomeCompleto.text = beneficiario.nomeCompleto ?: "Sem nome"

                // Tenta mostrar o email, se não houver, mostra o nº de estudante
                binding.tvDetalhe.text = beneficiario.email ?: beneficiario.numEstudante ?: "Sem contacto"

                // Lógica para o estado usando TextView estilizado como Chip
                val estado = beneficiario.estado ?: "inativo"
                val isAtivo = estado.equals("ativo", ignoreCase = true)
                
                binding.tvEstado.text = if (isAtivo) "Ativo" else "Inativo"
                
                // Configurar cor de fundo do TextView usando GradientDrawable
                try {
                    val colorRes = if (isAtivo) R.color.estadoAtivo else R.color.estadoInativo
                    val backgroundColor = ContextCompat.getColor(context, colorRes)
                    
                    // Criar um GradientDrawable programaticamente
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

                // Torna o item clicável e envia o ID
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

class BeneficiarioDiffCallback : DiffUtil.ItemCallback<Beneficiario>() {
    override fun areItemsTheSame(oldItem: Beneficiario, newItem: Beneficiario): Boolean {
        // IDs são suficientes para saber se são o mesmo item (mesmo que o conteúdo mude)
        return try {
            oldItem.id == newItem.id
        } catch (e: Exception) {
            android.util.Log.e("BeneficiarioDiffCallback", "Erro ao comparar items", e)
            false
        }
    }

    override fun areContentsTheSame(oldItem: Beneficiario, newItem: Beneficiario): Boolean {
        // Compara o conteúdo da data class (nomes, emails, etc.)
        return try {
            oldItem == newItem
        } catch (e: Exception) {
            android.util.Log.e("BeneficiarioDiffCallback", "Erro ao comparar conteúdos", e)
            false
        }
    }
}
