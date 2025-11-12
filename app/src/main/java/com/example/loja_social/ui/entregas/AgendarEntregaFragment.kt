package com.example.loja_social.ui.entregas

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
import com.example.loja_social.R
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentAgendarEntregaBinding
import com.example.loja_social.repository.AgendarEntregaRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate // Necessário para sugestão de data

class AgendarEntregaFragment : Fragment() {

    private var _binding: FragmentAgendarEntregaBinding? = null
    private val binding get() = _binding!!

    private var selectedBeneficiarioId: String? = null

    private val viewModel: AgendarEntregaViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = AgendarEntregaRepository(apiService)
        AgendarEntregaViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgendarEntregaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sugere a data de hoje por defeito
        binding.etDataAgendamento.setText(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Listener para a seleção do Beneficiário
        binding.actvBeneficiario.setOnItemClickListener { parent, _, position, _ ->
            viewModel.clearMessages()
            val selectedItem = parent.getItemAtPosition(position).toString()
            selectedBeneficiarioId = viewModel.uiState.value.beneficiarios.find {
                "${it.nomeCompleto} (${it.numEstudante})" == selectedItem
            }?.id
            // Ativa o botão após a seleção
            binding.btnAgendar.isEnabled = selectedBeneficiarioId != null
        }

        // Listener para o botão de agendamento
        binding.btnAgendar.setOnClickListener {
            val nomeCompleto = binding.actvBeneficiario.text.toString().trim()
            val dataAgendamento = binding.etDataAgendamento.text.toString().trim()

            if (selectedBeneficiarioId != null && dataAgendamento.isNotEmpty()) {
                viewModel.agendarEntrega(nomeCompleto, dataAgendamento)
            } else {
                Toast.makeText(context, "Selecione um beneficiário e uma data.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->

                // Loading State
                binding.progressBarOverlay.isVisible = state.isLoading || state.isScheduling
                binding.btnAgendar.text = if (state.isScheduling) "A Agendar..." else "AGENDAR ENTREGA"
                binding.btnAgendar.isEnabled = selectedBeneficiarioId != null && !state.isScheduling

                // Popula o Dropdown de Beneficiários (apenas uma vez)
                if (!state.isLoading && state.beneficiarios.isNotEmpty() && binding.actvBeneficiario.adapter == null) {
                    val nomes = state.beneficiarios.map {
                        // Formato: Nome Completo (N.º Estudante)
                        "${it.nomeCompleto} (${it.numEstudante ?: "N/A"})"
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nomes)
                    binding.actvBeneficiario.setAdapter(adapter)
                }

                // Messages
                if (state.errorMessage != null) {
                    binding.tvMessage.text = state.errorMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
                    binding.tvMessage.isVisible = true
                } else if (state.successMessage != null) {
                    binding.tvMessage.text = state.successMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoAtivo))
                    binding.tvMessage.isVisible = true
                    // Limpar formulário após sucesso
                    binding.actvBeneficiario.setText("")
                    selectedBeneficiarioId = null
                } else {
                    binding.tvMessage.isVisible = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}