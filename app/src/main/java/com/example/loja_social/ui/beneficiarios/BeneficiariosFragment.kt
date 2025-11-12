package com.example.loja_social.ui.beneficiarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loja_social.R
import com.example.loja_social.api.RetrofitHelper
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentBeneficiariosBinding
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.launch

class BeneficiariosFragment : Fragment() {

    private var _binding: FragmentBeneficiariosBinding? = null
    private val binding get() = _binding!!

    private val beneficiarioAdapter = BeneficiarioAdapter { beneficiarioId ->
        navigateToDetail(beneficiarioId)
    }

    private val viewModel: BeneficiariosViewModel by viewModels {
        // O RetrofitInstance já deve estar inicializado na Application (LojaSocialApp)
        // Mas adicionamos proteção
        val apiService = try {
            RetrofitInstance.api
        } catch (e: UninitializedPropertyAccessException) {
            // Se não estiver inicializado, lançar erro - será tratado no onViewCreated
            throw IllegalStateException("RetrofitInstance não inicializado. Deve ser inicializado na Application.", e)
        }
        val repository = BeneficiarioRepository(apiService)
        BeneficiariosViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeneficiariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Garantir que RetrofitInstance está inicializado
            if (!RetrofitHelper.ensureInitialized(requireContext())) {
                throw IllegalStateException("Não foi possível inicializar RetrofitInstance")
            }

            setupRecyclerView()
            setupSearchAndFilters()
            observeViewModel()

            // SwipeRefreshLayout
            binding.swipeRefresh.setOnRefreshListener {
                viewModel.fetchBeneficiarios()
            }

            // Listener para o botão de adicionar
            binding.fabAddBeneficiario.setOnClickListener {
                navigateToDetail(null) // ID nulo para criar
            }
        } catch (e: Exception) {
            android.util.Log.e("BeneficiariosFragment", "Erro no onViewCreated", e)
            // Mostrar mensagem de erro ao utilizador
            binding.cardError.isVisible = true
            binding.tvErro.text = getString(R.string.error_initializing, e.message ?: getString(R.string.error_unknown))
            binding.rvBeneficiarios.isVisible = false
            binding.emptyState.isVisible = false
            binding.progressBar.isVisible = false
        }
    }

    private fun setupRecyclerView() {
        binding.rvBeneficiarios.apply {
            adapter = beneficiarioAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupSearchAndFilters() {
        // Listener para pesquisa
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString()
                viewModel.setSearchQuery(query)
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                true
            } else {
                false
            }
        }

        // Pesquisa em tempo real (opcional - pode ser removido se preferir só no Enter)
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
        })

        // Filtros por estado
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilterState(null)
        }
        binding.chipAtivo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilterState("ativo")
        }
        binding.chipInativo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilterState("inativo")
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchBeneficiarios()
    }

    private fun observeViewModel() {
        // Observar loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading && !binding.swipeRefresh.isRefreshing
                binding.swipeRefresh.isRefreshing = isLoading
            }
        }

        // Observar lista de beneficiários
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { beneficiarios ->
                try {
                    beneficiarioAdapter.submitList(beneficiarios)
                    // Atualizar visibilidade baseado nos dados
                    updateVisibility()
                } catch (e: Exception) {
                    android.util.Log.e("BeneficiariosFragment", "Erro ao atualizar lista", e)
                    binding.cardError.isVisible = true
                    binding.tvErro.text = "Erro ao carregar beneficiários: ${e.message}"
                }
            }
        }

        // Observar erros
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMsg ->
                if (errorMsg != null) {
                    binding.cardError.isVisible = true
                    binding.tvErro.text = errorMsg
                    binding.rvBeneficiarios.isVisible = false
                    binding.emptyState.isVisible = false
                } else {
                    binding.cardError.isVisible = false
                    updateVisibility()
                }
            }
        }
    }

    private fun updateVisibility() {
        val hasData = viewModel.uiState.value.isNotEmpty()
        val hasError = viewModel.errorMessage.value != null
        val isLoading = viewModel.isLoading.value

        if (!isLoading && !hasError) {
            binding.rvBeneficiarios.isVisible = hasData
            binding.emptyState.isVisible = !hasData
        }
    }

    private fun navigateToDetail(beneficiarioId: String?) {
        val title = if (beneficiarioId == null) "Novo Beneficiário" else "Editar Beneficiário"

        val bundle = Bundle().apply {
            putString("beneficiarioId", beneficiarioId)
            putString("title", title)
        }

        findNavController().navigate(R.id.action_nav_beneficiarios_to_nav_beneficiario_detail, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}