package com.example.loja_social.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentDashboardBinding
import com.example.loja_social.repository.DashboardRepository
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // 1. Ligar o ViewModel
    private val viewModel: DashboardViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = DashboardRepository(apiService)
        DashboardViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        // 2. Observar as mudanças do ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Controlar visibilidade
                binding.progressBar.isVisible = state.isLoading && !binding.swipeRefresh.isRefreshing
                binding.swipeRefresh.isRefreshing = state.isLoading
                binding.cardAlertas.isVisible = !state.isLoading && state.errorMessage == null
                binding.cardEntregas.isVisible = !state.isLoading && state.errorMessage == null
                binding.cardError.isVisible = state.errorMessage != null

                // Preencher dados
                if (!state.isLoading && state.errorMessage == null) {
                    binding.tvAlertasCount.text = state.alertas.size.toString()
                    binding.tvEntregasCount.text = state.entregasAgendadasCount.toString()
                }

                // Mostrar erro
                if (state.errorMessage != null) {
                    binding.tvErro.text = state.errorMessage
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}