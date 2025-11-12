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
            val context = binding.root.context

            binding.tvNomeCompleto.text = beneficiario.nomeCompleto

            // Tenta mostrar o email, se não houver, mostra o nº de estudante
            binding.tvDetalhe.text = beneficiario.email ?: beneficiario.numEstudante ?: "Sem contacto"

            // Lógica para o estado
            if (beneficiario.estado.equals("ativo", ignoreCase = true)) {
                binding.tvEstado.text = "Ativo"
                binding.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.estadoAtivo))
                binding.tvEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.verdeFundo))
            } else {
                binding.tvEstado.text = "Inativo"
                binding.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.estadoInativo))
                binding.tvEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            // [NOVO] Torna o item clicável e envia o ID
            binding.root.setOnClickListener {
                onBeneficiarioClicked(beneficiario.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiarioViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemBeneficiarioBinding.inflate(inflater, parent, false)
        return BeneficiarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BeneficiarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class BeneficiarioDiffCallback : DiffUtil.ItemCallback<Beneficiario>() {
    override fun areItemsTheSame(oldItem: Beneficiario, newItem: Beneficiario): Boolean {
        // IDs são suficientes para saber se são o mesmo item (mesmo que o conteúdo mude)
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Beneficiario, newItem: Beneficiario): Boolean {
        // Compara o conteúdo da data class (nomes, emails, etc.)
        return oldItem == newItem
    }
}
