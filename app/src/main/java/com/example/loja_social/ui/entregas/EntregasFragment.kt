package com.example.loja_social.ui.entregas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat // <--- IMPORT ADICIONADO
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.loja_social.R // <--- IMPORT ADICIONADO
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

        // Observar o estado do ViewModel
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Controlar visibilidade
                binding.progressBar.isVisible = state.isLoading
                binding.rvEntregas.isVisible = !state.isLoading && state.errorMessage == null

                // Preencher dados na lista
                entregaAdapter.submitList(state.entregas)

                // Mostrar mensagem de sucesso ou erro (prioridade ao erro)
                if (state.errorMessage != null) {
                    binding.tvMessage.text = state.errorMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
                    binding.tvMessage.isVisible = true
                } else if (state.actionSuccessMessage != null) {
                    binding.tvMessage.text = state.actionSuccessMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoAtivo))
                    binding.tvMessage.isVisible = true
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