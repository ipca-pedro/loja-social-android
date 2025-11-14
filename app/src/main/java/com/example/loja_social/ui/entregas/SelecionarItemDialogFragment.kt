package com.example.loja_social.ui.entregas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.databinding.DialogSelecionarItemBinding

class SelecionarItemDialogFragment(
    private val lotesDisponiveis: List<LoteIndividual>,
    private val onItemSelected: (LoteIndividual, Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogSelecionarItemBinding? = null
    private val binding get() = _binding!!

    private var selectedLote: LoteIndividual? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelecionarItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdown()
        setupListeners()
    }

    private fun setupDropdown() {
        val nomesLotes = lotesDisponiveis.map {
            "${it.produto} (Disp: ${it.quantidadeAtual}, Val: ${it.dataValidade ?: "N/A"})"
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nomesLotes)
        binding.actvLote.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.actvLote.setOnItemClickListener { parent, _, position, _ ->
            val selectedItemString = parent.getItemAtPosition(position).toString()
            selectedLote = lotesDisponiveis.find {
                "${it.produto} (Disp: ${it.quantidadeAtual}, Val: ${it.dataValidade ?: "N/A"})" == selectedItemString
            }
        }

        binding.btnAdicionar.setOnClickListener {
            val quantidadeStr = binding.etQuantidadeDialog.text.toString()
            if (selectedLote != null && quantidadeStr.isNotEmpty()) {
                val quantidade = quantidadeStr.toInt()
                onItemSelected(selectedLote!!, quantidade)
                dismiss()
            } else {
                // TODO: Mostrar erro
            }
        }

        binding.btnCancelar.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}