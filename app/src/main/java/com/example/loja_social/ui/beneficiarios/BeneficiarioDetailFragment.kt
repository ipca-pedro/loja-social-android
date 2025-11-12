package com.example.loja_social.ui.beneficiarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.loja_social.R
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentBeneficiarioDetailBinding
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.launch

class BeneficiarioDetailFragment : Fragment() {

    private var _binding: FragmentBeneficiarioDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BeneficiarioDetailFragmentArgs by navArgs()

    private val viewModel: BeneficiarioDetailViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = BeneficiarioRepository(apiService)
        // Passa o ID diretamente. O ViewModel tratará da lógica de carregar.
        BeneficiarioDetailViewModelFactory(repository, args.beneficiarioId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeneficiarioDetailBinding.inflate(inflater, container, false)
        // Define o título aqui, pois a view já foi criada
        activity?.title = args.title
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEstadoSpinner()
        setupListeners()
        observeViewModel()

        binding.btnDelete.isVisible = args.beneficiarioId != null
    }

    private fun setupEstadoSpinner() {
        val estados = arrayOf("ativo", "inativo")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estados)
        binding.actvEstado.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener { saveChanges() }
        binding.btnDelete.setOnClickListener { deactivateBeneficiario() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Controla a visibilidade do ProgressBar e o estado dos botões
                binding.progressBar.isVisible = state.isLoading || state.isSaving
                binding.btnSave.isEnabled = !state.isSaving
                binding.btnDelete.isEnabled = args.beneficiarioId != null && !state.isSaving

                // Popula o formulário quando o beneficiário é carregado
                state.beneficiario?.let { populateForm(it) }

                // Exibe mensagens de sucesso ou erro
                handleMessages(state)
            }
        }
        // Observa o evento de navegação para fechar o fragmento
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateBack.collect {
                findNavController().popBackStack()
            }
        }
    }

    private fun handleMessages(state: BeneficiarioDetailUiState) {
        if (state.errorMessage != null) {
            binding.tvMessage.text = state.errorMessage
            binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
            binding.tvMessage.isVisible = true
        } else if (state.successMessage != null) {
            binding.tvMessage.text = state.successMessage
            binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoAtivo))
            binding.tvMessage.isVisible = true

            // Em modo de criação, limpa o formulário para permitir adicionar outro
            if (args.beneficiarioId == null) {
                clearForm()
            }
        } else {
            binding.tvMessage.isVisible = false
        }
    }

    private fun saveChanges() {
        val nome = binding.etNomeCompleto.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val estado = binding.actvEstado.text.toString().trim()

        if (nome.isEmpty() || email.isEmpty() || estado.isEmpty()) {
            binding.tvMessage.text = "Nome, Email e Estado são obrigatórios."
            binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
            binding.tvMessage.isVisible = true
            return
        }

        val request = BeneficiarioRequest(
            nomeCompleto = nome,
            email = email,
            estado = estado,
            numEstudante = binding.etNumEstudante.text.toString().trim().ifEmpty { null },
            nif = binding.etNif.text.toString().trim().ifEmpty { null },
            notasAdicionais = binding.etNotas.text.toString().trim().ifEmpty { null },
            // Campos não editáveis são preservados pelo ViewModel
            anoCurricular = viewModel.uiState.value.beneficiario?.anoCurricular,
            curso = viewModel.uiState.value.beneficiario?.curso,
            telefone = viewModel.uiState.value.beneficiario?.telefone
        )

        viewModel.saveBeneficiario(request)
    }

    private fun deactivateBeneficiario() {
        viewModel.deactivateBeneficiario()
    }

    private fun populateForm(b: Beneficiario) {
        // Evita repopular o formulário se já tiver texto (evita perder dados em re-criação)
        if (binding.etNomeCompleto.text.toString() != b.nomeCompleto) {
            binding.etNomeCompleto.setText(b.nomeCompleto)
            binding.etNumEstudante.setText(b.numEstudante)
            binding.etEmail.setText(b.email)
            binding.etNif.setText(b.nif)
            binding.etNotas.setText(b.notasAdicionais)
            binding.actvEstado.setText(b.estado, false)
        }
    }

    private fun clearForm() {
        // 1. Limpa os campos de texto usando o método setText("")
        binding.etNomeCompleto.setText("")
        binding.etNumEstudante.setText("")
        binding.etEmail.setText("")
        binding.etNif.setText("")
        binding.etNotas.setText("")

        // 2. Limpa o campo dropdown (já está correto)
        binding.actvEstado.setText("", false)

        // 3. Limpa a mensagem de sucesso/erro
        binding.tvMessage.isVisible = false

        // 4. Foco no primeiro campo
        binding.etNomeCompleto.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}