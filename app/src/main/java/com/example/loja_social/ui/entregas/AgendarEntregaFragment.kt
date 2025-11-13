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

/**
 * Fragment para agendar novas entregas.
 * Permite selecionar um beneficiário, uma data e múltiplos itens de stock para entrega.
 */
class AgendarEntregaFragment : Fragment() {

    private var _binding: FragmentAgendarEntregaBinding? = null
    private val binding get() = _binding!!

    /** ID do beneficiário selecionado no dropdown */
    private var selectedBeneficiarioId: String? = null

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Preenche a data com a data atual como sugestão
        binding.etDataAgendamento.setText(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))

        setupListeners()
        observeViewModel()
    }

    /**
     * Configura os listeners dos componentes da UI.
     * - Dropdown de beneficiários: atualiza o ID selecionado e habilita o botão
     * - Botão de agendamento: valida e envia os dados ao ViewModel
     */
    private fun setupListeners() {
        // Listener para a seleção do Beneficiário no AutoCompleteTextView
        binding.actvBeneficiario.setOnItemClickListener { parent, _, position, _ ->
            viewModel.clearMessages()
            val selectedItem = parent.getItemAtPosition(position).toString()

            // Encontra o beneficiário correspondente ao item selecionado
            // O formato do display é: "Nome Completo (N.º Estudante)" ou "Nome Completo (N/A)" se não houver número
            selectedBeneficiarioId = viewModel.uiState.value.beneficiarios.find { beneficiario ->
                val displayString = "${beneficiario.nomeCompleto} (${beneficiario.numEstudante ?: "N/A"})"
                displayString == selectedItem
            }?.id

            // Habilita o botão apenas se um beneficiário foi selecionado
            binding.btnAgendar.isEnabled = selectedBeneficiarioId != null
        }

        // Listener para o botão de agendamento
        binding.btnAgendar.setOnClickListener {
            val dataAgendamento = binding.etDataAgendamento.text.toString().trim()

            if (selectedBeneficiarioId != null && dataAgendamento.isNotEmpty()) {
                // O '!!' é seguro porque verificamos que selectedBeneficiarioId != null no 'if' acima
                viewModel.agendarEntrega(selectedBeneficiarioId!!, dataAgendamento)
            } else {
                Toast.makeText(context, "Selecione um beneficiário e uma data.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Observa as mudanças do estado do ViewModel e atualiza a UI.
     * Gerencia:
     * - Estados de loading (carregamento inicial e agendamento)
     * - População do dropdown de beneficiários (apenas uma vez)
     * - Exibição de mensagens de erro e sucesso
     * - Limpeza do formulário após sucesso
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->

                // Estado de loading: mostra progress bar e desabilita botão durante carregamento/agendamento
                binding.progressBarOverlay.isVisible = state.isLoading || state.isScheduling
                binding.btnAgendar.text = if (state.isScheduling) "A Agendar..." else "AGENDAR ENTREGA"
                binding.btnAgendar.isEnabled = selectedBeneficiarioId != null && !state.isScheduling

                // Popula o dropdown de beneficiários apenas uma vez quando os dados estão prontos
                if (!state.isLoading && state.beneficiarios.isNotEmpty() && binding.actvBeneficiario.adapter == null) {
                    val nomes = state.beneficiarios.map {
                        // Formato exibido: "Nome Completo (N.º Estudante)" ou "Nome Completo (N/A)"
                        "${it.nomeCompleto} (${it.numEstudante ?: "N/A"})"
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nomes)
                    binding.actvBeneficiario.setAdapter(adapter)
                }

                // Gerencia mensagens de feedback (erro ou sucesso)
                if (state.errorMessage != null) {
                    binding.tvMessage.text = state.errorMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
                    binding.tvMessage.isVisible = true
                } else if (state.successMessage != null) {
                    binding.tvMessage.text = state.successMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoAtivo))
                    binding.tvMessage.isVisible = true
                    // Limpa o formulário após sucesso para permitir novo agendamento
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