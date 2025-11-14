package com.example.loja_social.ui.entregas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.databinding.DialogSelecionarItemBinding

/**
 * DialogFragment para selecionar um lote de stock e quantidade a adicionar à entrega.
 * Exibe um dropdown com todos os lotes disponíveis e um campo para inserir a quantidade.
 * 
 * @param lotesDisponiveis Lista de lotes de stock disponíveis para seleção
 * @param onItemSelected Callback chamado quando um item é selecionado (recebe o lote e a quantidade)
 */
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

    /**
     * Configura o dropdown com a lista de lotes disponíveis.
     * Formato: "Produto (Disp: X, Val: Y)"
     */
    private fun setupDropdown() {
        val nomesLotes = lotesDisponiveis.map {
            "${it.produto} (Disp: ${it.quantidadeAtual}, Val: ${it.dataValidade ?: "N/A"})"
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nomesLotes)
        binding.actvLote.setAdapter(adapter)
    }

    /**
     * Configura os listeners dos botões e do dropdown.
     * Valida os dados antes de adicionar o item.
     */
    private fun setupListeners() {
        binding.actvLote.setOnItemClickListener { parent, _, position, _ ->
            val selectedItemString = parent.getItemAtPosition(position).toString()
            selectedLote = lotesDisponiveis.find {
                "${it.produto} (Disp: ${it.quantidadeAtual}, Val: ${it.dataValidade ?: "N/A"})" == selectedItemString
            }
            // Limpa mensagens de erro quando um lote é selecionado
            binding.tilLote.error = null
        }

        binding.btnAdicionar.setOnClickListener {
            val quantidadeStr = binding.etQuantidadeDialog.text.toString().trim()
            
            // Validação: verifica se um lote foi selecionado
            if (selectedLote == null) {
                binding.tilLote.error = "Selecione um lote"
                Toast.makeText(requireContext(), "Por favor, selecione um lote de produto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação: verifica se a quantidade foi preenchida
            if (quantidadeStr.isEmpty()) {
                binding.tilQuantidade.error = "Insira a quantidade"
                Toast.makeText(requireContext(), "Por favor, insira a quantidade", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação: verifica se a quantidade é um número válido
            val quantidade = quantidadeStr.toIntOrNull()
            if (quantidade == null || quantidade <= 0) {
                binding.tilQuantidade.error = "Quantidade inválida"
                Toast.makeText(requireContext(), "A quantidade deve ser um número maior que zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação: verifica se a quantidade não excede o disponível
            if (quantidade > selectedLote!!.quantidadeAtual) {
                binding.tilQuantidade.error = "Quantidade excede o disponível (${selectedLote!!.quantidadeAtual})"
                Toast.makeText(
                    requireContext(), 
                    "A quantidade não pode exceder ${selectedLote!!.quantidadeAtual} unidades disponíveis", 
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            
            // Todas as validações passaram, adiciona o item
            onItemSelected(selectedLote!!, quantidade)
            dismiss()
        }

        binding.btnCancelar.setOnClickListener {
            dismiss()
        }
        
        // Limpa erros quando o utilizador começa a digitar
        binding.etQuantidadeDialog.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilQuantidade.error = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}