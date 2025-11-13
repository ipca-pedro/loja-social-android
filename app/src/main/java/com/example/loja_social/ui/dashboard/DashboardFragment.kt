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

/**
 * Fragment do dashboard principal.
 * Exibe resumo de alertas de validade e contagem de entregas agendadas.
 * Suporta pull-to-refresh para atualizar os dados.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

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

        // Configura pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        // Observa as mudanças do ViewModel e atualiza a UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Controla visibilidade dos componentes baseado no estado
                binding.progressBar.isVisible = state.isLoading && !binding.swipeRefresh.isRefreshing
                binding.swipeRefresh.isRefreshing = state.isLoading
                binding.cardAlertas.isVisible = !state.isLoading && state.errorMessage == null
                binding.cardEntregas.isVisible = !state.isLoading && state.errorMessage == null
                binding.cardError.isVisible = state.errorMessage != null

                // Preenche dados quando carregamento termina com sucesso
                if (!state.isLoading && state.errorMessage == null) {
                    binding.tvAlertasCount.text = state.alertas.size.toString()
                    binding.tvEntregasCount.text = state.entregasAgendadasCount.toString()
                }

                // Exibe mensagem de erro se houver
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