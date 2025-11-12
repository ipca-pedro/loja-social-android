package com.example.loja_social.ui.beneficiarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.loja_social.R
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentBeneficiarioDetailBinding
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BeneficiarioDetailFragment : Fragment() {

    private var _binding: FragmentBeneficiarioDetailBinding? = null
    private val binding get() = _binding!!

    // Usamos navArgs para obter os argumentos do nav_graph (ID e título)
    private val args: BeneficiarioDetailFragmentArgs by navArgs()

    // Acedemos ao ViewModel da lista para obter a cache de dados (BeneficiariosFragment's ViewModel)
    // Nota: Em grandes apps, usaria-se um Shared ViewModel ou HILT/Koin. Esta é a solução mais prática.
    private val listViewModel: BeneficiariosViewModel by activityViewModels() // <-- Acedemos ao ViewModel da lista

    // O ViewModel deste fragmento (Detalhe)
    private val viewModel: BeneficiarioDetailViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = BeneficiarioRepository(apiService)

        // Passamos o repositório, a lista de cache (esperando que esteja carregada) e o ID
        val cachedList = listViewModel.uiState.value // Usamos o valor atual da lista
        BeneficiarioDetailViewModelFactory(repository, cachedList, args.beneficiarioId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Atualiza o título da Activity (se necessário)
        activity?.title = args.title
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeneficiarioDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEstadoSpinner()
        setupListeners()
        observeViewModel()
    }

    private fun setupEstadoSpinner() {
        val estados = arrayOf("ativo", "inativo")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estados)
        binding.actvEstado.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        // 1. Validação simples
        val nome = binding.etNomeCompleto.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val estado = binding.actvEstado.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty()) {
            binding.tvMessage.text = "Nome e Email são obrigatórios."
            binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
            binding.tvMessage.isVisible = true
            return
        }

        if (estado.isEmpty()) {
            binding.tvMessage.text = "O campo Estado é obrigatório (ativo ou inativo)."
            binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
            binding.tvMessage.isVisible = true
            return
        }

        // 2. Construir o Request Object
        val request = BeneficiarioRequest(
            nomeCompleto = nome,
            email = email,
            estado = estado,
            // Campos restantes:
            numEstudante = binding.etNumEstudante.text.toString().trim().ifEmpty { null },
            nif = binding.etNif.text.toString().trim().ifEmpty { null },
            notasAdicionais = binding.etNotas.text.toString().trim().ifEmpty { null },
            // Os campos abaixo não estão no formulário completo (simplificação), mas a API Node.js aceita null
            anoCurricular = null,
            curso = null,
            telefone = null
        )

        // 3. Enviar para o ViewModel
        viewModel.saveBeneficiario(request)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->

                // Controla Loading e o botão Salvar
                binding.progressBar.isVisible = state.isLoading || state.isSaving
                binding.btnSave.isEnabled = !state.isSaving

                // 1. Carregar dados (apenas se for a primeira vez e tivermos um beneficiário)
                if (!state.isLoading && state.beneficiario != null && binding.etNomeCompleto.text.isNullOrEmpty()) {
                    populateForm(state.beneficiario)
                }

                // 2. Mensagens
                if (state.errorMessage != null) {
                    binding.tvMessage.text = state.errorMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
                    binding.tvMessage.isVisible = true
                } else if (state.successMessage != null) {
                    binding.tvMessage.text = state.successMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoAtivo))
                    binding.tvMessage.isVisible = true

                    // Se estiver a criar (ID null), limpamos o formulário para criar o próximo
                    if (args.beneficiarioId == null) {
                        clearForm()
                    }
                    // Opcional: Navegar de volta após o sucesso (melhor prática para um fluxo normal)
                    // findNavController().popBackStack()
                } else {
                    binding.tvMessage.isVisible = false
                }
            }
        }
    }

    private fun populateForm(b: Beneficiario) {
        binding.etNomeCompleto.setText(b.nomeCompleto)
        binding.etNumEstudante.setText(b.numEstudante)
        binding.etEmail.setText(b.email)
        binding.etNif.setText(b.nif)
        binding.etNotas.setText(b.notasAdicionais)
        // Definir o texto no AutoCompleteTextView sem abrir o dropdown
        binding.actvEstado.setText(b.estado, false)
    }

    private fun clearForm() {
        binding.etNomeCompleto.setText("")
        binding.etNumEstudante.setText("")
        binding.etEmail.setText("")
        binding.etNif.setText("")
        binding.etNotas.setText("")
        binding.actvEstado.setText("", false)
        binding.etNomeCompleto.requestFocus() // Coloca o cursor no primeiro campo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}