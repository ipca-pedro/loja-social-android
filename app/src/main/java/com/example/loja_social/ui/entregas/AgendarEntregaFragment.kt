package com.example.loja_social.ui.entregas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loja_social.R
import com.example.loja_social.SessionManager
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentAgendarEntregaBinding
import com.example.loja_social.repository.AgendarEntregaRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar

class AgendarEntregaFragment : Fragment() {

    private var _binding: FragmentAgendarEntregaBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var agendarEntregaAdapter: AgendarEntregaAdapter
    // A propriedade beneficiariosAdapter foi REMOVIDA

    private val viewModel: AgendarEntregaViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = AgendarEntregaRepository(apiService)
        val stockRepository = com.example.loja_social.repository.StockRepository(apiService)
        AgendarEntregaViewModelFactory(repository, stockRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgendarEntregaBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeUiState()
        observeUiEvents()
    }

    override fun onResume() {
        super.onResume()
        binding.actvBeneficiario.setText("", false)
        viewModel.onFragmentReady()
    }

    private fun setupRecyclerView() {
        agendarEntregaAdapter = AgendarEntregaAdapter(
            onRemoveClicked = { loteId -> viewModel.removerItem(loteId) },
            onQuantityChanged = { loteId, novaQuantidade -> viewModel.atualizarQuantidade(loteId, novaQuantidade) }
        )
        binding.rvItensEntrega.apply {
            adapter = agendarEntregaAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupListeners() {
        binding.etDataAgendamento.setOnClickListener { showDatePickerDialog() }

        binding.actvBeneficiario.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            viewModel.onBeneficiarioSelected(selectedItem)
        }

        binding.btnAgendar.setOnClickListener {
            val colaboradorId = sessionManager.fetchColaboradorId()
            val dataAgendamento = binding.etDataAgendamento.text.toString().trim()
            if (colaboradorId != null) {
                viewModel.agendarEntrega(colaboradorId, dataAgendamento)
            } else {
                Toast.makeText(context, "Sessão de colaborador inválida.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddItem.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.errorMessage != null) {
                Toast.makeText(context, "Erro: ${state.errorMessage}", Toast.LENGTH_LONG).show()
            } else if (state.lotesDisponiveis.isNotEmpty()) {
                SelecionarItemDialogFragment(state.lotesDisponiveis) { lote, quantidade ->
                    viewModel.adicionarItem(lote, quantidade)
                }.show(parentFragmentManager, "SelecionarItemDialog")
            } else {
                Toast.makeText(context, "Não há lotes de stock disponíveis.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            binding.etDataAgendamento.setText(String.format("%02d/%02d/%d", d, m + 1, y))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                agendarEntregaAdapter.submitList(state.itensSelecionados)
                binding.progressBarOverlay.isVisible = state.isLoading || state.isScheduling
                binding.btnAgendar.text = if (state.isScheduling) "A Agendar..." else "AGENDAR ENTREGA"
                binding.btnAgendar.isEnabled = state.selectedBeneficiarioId != null && !state.isScheduling

                // ** LÓGICA FINAL E ROBUSTA **
                // Cria um novo adapter sempre que os dados mudam. É a forma mais segura para um AutoCompleteTextView.
                val nomes = state.beneficiarios.map { "${it.nomeCompleto} (${it.numEstudante ?: "N/A"})" }
                val newAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nomes)
                binding.actvBeneficiario.setAdapter(newAdapter)

                binding.cardMessage.isVisible = state.errorMessage != null
                if (state.errorMessage != null) {
                    binding.tvMessage.text = state.errorMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
                    binding.cardMessage.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_background))
                }
            }
        }
    }

    private fun observeUiEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is AgendarEntregaEvent.ShowSuccessMessage -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        binding.actvBeneficiario.setText("", false)
                        binding.etDataAgendamento.setText("")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}