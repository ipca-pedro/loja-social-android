package com.example.loja_social.ui.stock

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loja_social.R
import com.example.loja_social.api.LoteIndividual
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentStockDetailBinding
import com.example.loja_social.repository.StockRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StockDetailFragment : Fragment() {

    private var _binding: FragmentStockDetailBinding? = null
    private val binding get() = _binding!!

    private val args: StockDetailFragmentArgs by navArgs()

    private val loteAdapter = LoteAdapter(
        onEditClick = { lote -> showEditLoteDialog(lote) },
        onDeleteClick = { lote -> showDeleteConfirmation(lote) }
    )

    private val viewModel: StockDetailViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = StockRepository(apiService)
        StockDetailViewModelFactory(repository, args.produtoId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockDetailBinding.inflate(inflater, container, false)
        activity?.title = args.produtoNome
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvLotes.apply {
            adapter = loteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading

                if (state.stockItem != null) {
                    populateDetails(state.stockItem)
                }

                // Atualizar lista de lotes
                loteAdapter.submitList(state.lotes)
                binding.rvLotes.isVisible = state.lotes.isNotEmpty() && !state.isLoading
                binding.emptyLotes.isVisible = state.lotes.isEmpty() && !state.isLoading && state.errorMessage == null

                // Mensagens
                if (state.errorMessage != null) {
                    binding.cardError.isVisible = true
                    binding.tvError.text = state.errorMessage
                    binding.cardSuccess.isVisible = false
                } else if (state.successMessage != null) {
                    binding.cardSuccess.isVisible = true
                    binding.tvSuccess.text = state.successMessage
                    binding.cardError.isVisible = false
                    // Limpar mensagem após 3 segundos
                    viewLifecycleOwner.lifecycleScope.launch {
                        kotlinx.coroutines.delay(3000)
                        viewModel.clearMessages()
                    }
                } else {
                    binding.cardError.isVisible = false
                    binding.cardSuccess.isVisible = false
                }
            }
        }
    }

    private fun populateDetails(stockItem: com.example.loja_social.api.StockItem) {
        binding.tvProdutoNome.text = stockItem.produto
        binding.tvCategoria.text = stockItem.categoria ?: "Sem categoria"
        binding.tvQuantidadeTotal.text = "${stockItem.quantidadeTotal} unidades"
        binding.tvNumLotes.text = "${stockItem.lotes} ${if (stockItem.lotes == 1) "lote" else "lotes"}"

        if (stockItem.validadeProxima != null) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(stockItem.validadeProxima)
                if (date != null) {
                    binding.tvValidadeProxima.text = outputFormat.format(date)
                    binding.llValidade.isVisible = true
                } else {
                    binding.llValidade.isVisible = false
                }
            } catch (e: Exception) {
                binding.tvValidadeProxima.text = stockItem.validadeProxima
                binding.llValidade.isVisible = true
            }
        } else {
            binding.llValidade.isVisible = false
        }
    }

    private fun showEditLoteDialog(lote: LoteIndividual) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_lote, null)
        val quantidadeEditText = dialogView.findViewById<android.widget.EditText>(R.id.et_quantidade_atual)
        val dataValidadeEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_data_validade)
        val tilDataValidade = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.til_data_validade)

        quantidadeEditText.setText(lote.quantidadeAtual.toString())
        
        if (lote.dataValidade != null) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(lote.dataValidade)
                if (date != null) {
                    dataValidadeEditText.setText(outputFormat.format(date))
                }
            } catch (e: Exception) {
                dataValidadeEditText.setText(lote.dataValidade)
            }
        }

        dataValidadeEditText.setOnClickListener { showDatePickerForEdit(dataValidadeEditText) }
        tilDataValidade.setEndIconOnClickListener { showDatePickerForEdit(dataValidadeEditText) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar Lote")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val quantidadeStr = quantidadeEditText.text.toString()
                val dataValidadeStr = dataValidadeEditText.text.toString().trim()
                
                val quantidade = quantidadeStr.toIntOrNull()
                if (quantidade == null || quantidade < 0) {
                    Toast.makeText(context, "Quantidade inválida", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (quantidade > lote.quantidadeInicial) {
                    Toast.makeText(context, "Quantidade atual não pode ser maior que a inicial (${lote.quantidadeInicial})", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                // Converter data de DD/MM/yyyy para yyyy-MM-dd
                val dataFormatada = if (dataValidadeStr.isNotEmpty()) {
                    try {
                        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = inputFormat.parse(dataValidadeStr)
                        if (date != null) {
                            outputFormat.format(date)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }

                viewModel.updateLote(lote.id, quantidade, dataFormatada)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDatePickerForEdit(editText: com.google.android.material.textfield.TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )
                editText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun showDeleteConfirmation(lote: LoteIndividual) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar Remoção")
            .setMessage("Tem certeza que deseja remover este lote?\n\nQuantidade: ${lote.quantidadeAtual} unidades\n\nEsta ação não pode ser desfeita.")
            .setPositiveButton("Remover") { _, _ ->
                viewModel.deleteLote(lote.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

