package com.example.loja_social.ui.entregas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController // <--- IMPORT ESSENCIAL CORRIGIDO
import com.example.loja_social.R
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentEntregasBinding
import com.example.loja_social.repository.EntregaRepository
import kotlinx.coroutines.launch

class EntregasFragment : Fragment() {

    private var _binding: FragmentEntregasBinding? = null
    private val binding get() = _binding!!

    // Inicializar o Adapter com a lambda para o clique do botão
    private val entregaAdapter = EntregaAdapter { entregaId ->
        viewModel.concluirEntrega(entregaId)
    }

    private val viewModel: EntregasViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = EntregaRepository(apiService)
        EntregasViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntregasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar o RecyclerView
        binding.rvEntregas.adapter = entregaAdapter

        // SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchEntregas()
        }

        // LÓGICA DO BOTÃO FAB PARA AGENDAR (PROBLEMA DE NAVEGAÇÃO CORRIGIDO PELO IMPORT)
        binding.fabAddEntrega.setOnClickListener {
            // Navega para o novo fragmento de agendamento
            findNavController().navigate(R.id.action_nav_entregas_to_nav_agendar_entrega)
        }

        // Observar o estado do ViewModel
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Controlar visibilidade
                binding.progressBar.isVisible = state.isLoading && !binding.swipeRefresh.isRefreshing
                binding.swipeRefresh.isRefreshing = state.isLoading

                // Lógica para mostrar a lista ou o empty state
                val hasData = state.entregas.isNotEmpty()
                val showList = !state.isLoading && state.errorMessage == null && hasData
                val showEmptyState = !state.isLoading && state.errorMessage == null && !hasData

                binding.rvEntregas.isVisible = showList
                binding.emptyState.isVisible = showEmptyState
                binding.fabAddEntrega.isVisible = !state.isLoading

                // Preencher dados na lista
                entregaAdapter.submitList(state.entregas)

                // Mostrar mensagem de sucesso ou erro (usando Snackbar seria melhor, mas mantendo compatibilidade)
                if (state.actionSuccessMessage != null) {
                    // Mostrar mensagem de sucesso temporariamente
                    android.widget.Toast.makeText(
                        requireContext(),
                        state.actionSuccessMessage,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}