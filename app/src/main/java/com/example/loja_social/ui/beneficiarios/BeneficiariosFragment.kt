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

    private val beneficiarioAdapter = BeneficiarioAdapter { beneficiarioId ->
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

        setupRecyclerView()
        observeViewModel()

        // Listener para o botão de adicionar
        binding.fabAddBeneficiario.setOnClickListener {
            navigateToDetail(null) // ID nulo para criar
        }
    }

    private fun setupRecyclerView() {
        binding.rvBeneficiarios.apply {
            adapter = beneficiarioAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchBeneficiarios()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
                binding.rvBeneficiarios.isVisible = !isLoading
                // O botão de adicionar só aparece depois do carregamento inicial
                binding.fabAddBeneficiario.isVisible = !isLoading
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
                binding.tvErro.isVisible = errorMsg != null
                // Esconde a lista e o botão se houver um erro
                if (errorMsg != null) {
                    binding.rvBeneficiarios.isVisible = false
                    binding.fabAddBeneficiario.isVisible = false
                }
            }
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