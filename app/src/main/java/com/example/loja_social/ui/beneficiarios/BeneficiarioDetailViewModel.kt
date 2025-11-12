package com.example.loja_social.ui.beneficiarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loja_social.R
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentBeneficiariosBinding
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.launch

class BeneficiariosFragment : Fragment() {

    private var _binding: FragmentBeneficiariosBinding? = null
    private val binding get() = _binding!!

    // 1. Inicializa o Adapter com a lógica de navegação
    private val beneficiarioAdapter = BeneficiarioAdapter { beneficiarioId ->
        // Lógica de clique para navegar para o detalhe
        navigateToDetail(beneficiarioId)
    }

    private val viewModel: BeneficiariosViewModel by viewModels {
        val apiService = RetrofitInstance.api
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

        // Configurar o RecyclerView
        binding.rvBeneficiarios.apply {
            adapter = beneficiarioAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // [NOVO] Adicionar o botão para CRIAR um novo beneficiário
        binding.fabAddBeneficiario.setOnClickListener {
            navigateToDetail(null)
        }

        observeViewModel()
    }

    // [NOVO] Recarregar a lista sempre que o ecrã volta à frente
    override fun onResume() {
        super.onResume()
        // Garante que a lista é recarregada sempre que o utilizador regressa a este fragmento
        viewModel.fetchBeneficiarios()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
                // O FAB só deve aparecer quando não está a carregar
                binding.fabAddBeneficiario.isVisible = !isLoading
                binding.rvBeneficiarios.isVisible = !isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { beneficiarios ->
                beneficiarioAdapter.submitList(beneficiarios)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMsg ->
                binding.tvErro.text = errorMsg
                binding.tvErro.isVisible = (errorMsg != null)
                if (errorMsg != null) {
                    binding.rvBeneficiarios.isVisible = false
                }
            }
        }
    }

    // Função de navegação
    private fun navigateToDetail(beneficiarioId: String?) {
        val title = if (beneficiarioId == null) "Novo Beneficiário" else "Editar Beneficiário"

        // 1. Cria um Bundle com os argumentos
        val bundle = Bundle().apply {
            // O nome do argumento TEM de corresponder ao do nav_graph.xml: "beneficiarioId"
            putString("beneficiarioId", beneficiarioId)
            putString("title", title)
        }

        // 2. Navega usando o ID do destino (nav_beneficiario_detail)
        findNavController().navigate(R.id.nav_beneficiario_detail, bundle)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}