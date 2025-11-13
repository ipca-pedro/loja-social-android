package com.example.loja_social.ui.beneficiarios

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.loja_social.R
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.BeneficiarioRequest
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentBeneficiarioDetailBinding
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fragment para criar ou editar um beneficiário.
 * Suporta dois modos: criação (beneficiarioId == null) e edição (beneficiarioId != null).
 * Inclui validação de campos, formatação de dados e feedback visual.
 */
class BeneficiarioDetailFragment : Fragment() {

    private var _binding: FragmentBeneficiarioDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BeneficiarioDetailFragmentArgs by navArgs()

    private val viewModel: BeneficiarioDetailViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = BeneficiarioRepository(apiService)
        // Passa o ID do beneficiário (null em modo criação, não-null em modo edição)
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

        val isEditing = args.beneficiarioId != null
        binding.btnDelete.isVisible = isEditing
        // Estado só é visível na edição (na criação, o beneficiário é criado como "ativo" por padrão na API)
        binding.tilEstado.isVisible = isEditing
        
        // Ajustar texto do botão e garantir que está habilitado
        if (!isEditing) {
            binding.btnSave.text = "CRIAR BENEFICIÁRIO"
            binding.btnSave.isEnabled = true
            android.util.Log.d("BeneficiarioDetail", "Modo de criação - botão habilitado e texto atualizado")
        } else {
            binding.btnSave.text = "SALVAR ALTERAÇÕES"
        }
    }

    /**
     * Configura o dropdown de estado (ativo/inativo).
     * Apenas visível em modo edição.
     */
    private fun setupEstadoSpinner() {
        val estados = arrayOf("ativo", "inativo")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, estados)
        binding.actvEstado.setAdapter(adapter)
    }

    /**
     * Configura os listeners dos botões.
     * - Botão Salvar: valida e envia os dados
     * - Botão Remover: desativa o beneficiário (apenas em modo edição)
     */
    private fun setupListeners() {
        binding.btnSave.setOnClickListener { 
            android.util.Log.d("BeneficiarioDetail", "Botão SALVAR clicado! Enabled=${binding.btnSave.isEnabled}")
            if (!binding.btnSave.isEnabled) {
                Toast.makeText(requireContext(), "Aguarde...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveChanges() 
        }
        binding.btnDelete.setOnClickListener { deactivateBeneficiario() }
    }

    /**
     * Observa as mudanças do ViewModel e atualiza a UI.
     * Gerencia estados de loading, população do formulário, mensagens e navegação.
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Controla visibilidade do ProgressBar e estado dos botões
                binding.progressBar.isVisible = state.isLoading || state.isSaving
                val wasEnabled = binding.btnSave.isEnabled
                binding.btnSave.isEnabled = !state.isSaving
                if (wasEnabled != binding.btnSave.isEnabled) {
                    android.util.Log.d("BeneficiarioDetail", "Estado do botão mudou: enabled=${binding.btnSave.isEnabled}, isSaving=${state.isSaving}")
                }
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

    /**
     * Gerencia a exibição de mensagens de erro e sucesso.
     * Em modo criação, limpa o formulário após sucesso para permitir adicionar outro.
     * @param state Estado atual do ViewModel
     */
    private fun handleMessages(state: BeneficiarioDetailUiState) {
        if (state.errorMessage != null) {
            android.util.Log.w("BeneficiarioDetail", "Mostrando mensagem de erro: ${state.errorMessage}")
            binding.tvMessage.text = state.errorMessage
            binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
            binding.cardMessage.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            binding.tvMessage.isVisible = true
            binding.cardMessage.isVisible = true
        } else if (state.successMessage != null) {
            android.util.Log.d("BeneficiarioDetail", "Mostrando mensagem de sucesso: ${state.successMessage}")
            binding.tvMessage.text = state.successMessage
            binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            binding.cardMessage.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
            binding.tvMessage.isVisible = true
            binding.cardMessage.isVisible = true

            // Em modo de criação, limpa o formulário para permitir adicionar outro
            if (args.beneficiarioId == null && state.successMessage != null) {
                // Pequeno delay para o utilizador ver a mensagem de sucesso
                viewLifecycleOwner.lifecycleScope.launch {
                    kotlinx.coroutines.delay(2000)
                    clearForm()
                    binding.cardMessage.isVisible = false
                }
            }
        } else {
            binding.tvMessage.isVisible = false
            binding.cardMessage.isVisible = false
        }
    }

    /**
     * Valida e salva os dados do formulário.
     * Aplica validações de campos obrigatórios, formato de email e NIF.
     * Cria BeneficiarioRequest e envia ao ViewModel.
     */
    private fun saveChanges() {
        android.util.Log.d("BeneficiarioDetail", "saveChanges() chamado")
        val nome = binding.etNomeCompleto.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val estado = binding.actvEstado.text.toString().trim()
        val isEditing = args.beneficiarioId != null

        android.util.Log.d("BeneficiarioDetail", "Dados do formulário: nome='$nome', email='$email', estado='$estado', isEditing=$isEditing")
        
        // Feedback visual imediato
        Toast.makeText(requireContext(), "A processar...", Toast.LENGTH_SHORT).show()

        // Validação: Nome e Email são sempre obrigatórios
        if (nome.isEmpty() || email.isEmpty()) {
            val errorMsg = "Nome e Email são obrigatórios."
            android.util.Log.w("BeneficiarioDetail", "Validação falhou: $errorMsg")
            showErrorMessage(errorMsg)
            return
        }
        
        // Validação de formato de email básica
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            val errorMsg = "Por favor, insira um email válido."
            android.util.Log.w("BeneficiarioDetail", "Validação de email falhou")
            showErrorMessage(errorMsg)
            return
        }
        
        // Validação de NIF (deve ter 9 dígitos se fornecido)
        val nif = binding.etNif.text.toString().trim()
        if (nif.isNotEmpty() && (nif.length != 9 || !nif.all { it.isDigit() })) {
            val errorMsg = "O NIF deve ter exatamente 9 dígitos."
            android.util.Log.w("BeneficiarioDetail", "Validação de NIF falhou")
            showErrorMessage(errorMsg)
            return
        }

        // Estado só é obrigatório na edição
        if (isEditing && estado.isEmpty()) {
            val errorMsg = "Estado é obrigatório ao editar."
            android.util.Log.w("BeneficiarioDetail", "Validação falhou: $errorMsg")
            showErrorMessage(errorMsg)
            return
        }
        
        // Se chegou aqui, a validação passou - esconder mensagens anteriores
        binding.cardMessage.isVisible = false

        // Cria a requisição com os dados do formulário
        // Nota: Em modo edição, preserva campos não editáveis (anoCurricular, curso, telefone)
        val request = BeneficiarioRequest(
            nomeCompleto = nome,
            email = email,
            // Estado só é enviado na edição (a API cria como "ativo" por padrão)
            estado = if (isEditing) estado else null,
            numEstudante = binding.etNumEstudante.text.toString().trim().ifEmpty { null },
            nif = binding.etNif.text.toString().trim().ifEmpty { null },
            notasAdicionais = binding.etNotas.text.toString().trim().ifEmpty { null },
            // Na edição, preserva os campos não editáveis do beneficiário existente
            // Na criação, esses campos são null (a API aceita null)
            anoCurricular = if (isEditing) viewModel.uiState.value.beneficiario?.anoCurricular else null,
            curso = if (isEditing) viewModel.uiState.value.beneficiario?.curso else null,
            telefone = if (isEditing) viewModel.uiState.value.beneficiario?.telefone else null
        )
        
        android.util.Log.d("BeneficiarioDetail", "Criando/Editando beneficiário: isEditing=$isEditing, request=$request")

        viewModel.saveBeneficiario(request)
    }

    /**
     * Desativa o beneficiário (muda estado para "inativo").
     * Apenas disponível em modo edição.
     */
    private fun deactivateBeneficiario() {
        viewModel.deactivateBeneficiario()
    }

    /**
     * Popula o formulário com os dados do beneficiário.
     * Evita repopular se já tiver texto (previne perda de dados durante re-criação da view).
     * @param b O beneficiário a exibir
     */
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

    /**
     * Exibe uma mensagem de erro no card de mensagens.
     * @param message A mensagem de erro a exibir
     */
    private fun showErrorMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.cardMessage.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        binding.tvMessage.isVisible = true
        binding.cardMessage.isVisible = true
    }

    /**
     * Limpa todos os campos do formulário.
     * Usado após criar um beneficiário com sucesso para permitir adicionar outro.
     */
    private fun clearForm() {
        // Limpa os campos de texto
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