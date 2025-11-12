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
                binding.progressBar.isVisible = state.isLoading

                // Lógica para mostrar a lista ou a mensagem de lista vazia
                val showList = !state.isLoading && state.errorMessage == null && state.entregas.isNotEmpty()
                val showEmptyMessage = !state.isLoading && state.errorMessage == null && state.entregas.isEmpty()

                binding.rvEntregas.isVisible = showList
                binding.fabAddEntrega.isVisible = !state.isLoading

                // Preencher dados na lista
                entregaAdapter.submitList(state.entregas)

                // Mostrar mensagem de sucesso, erro, ou lista vazia
                if (state.errorMessage != null) {
                    binding.tvMessage.text = state.errorMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
                    binding.tvMessage.isVisible = true
                } else if (state.actionSuccessMessage != null) {
                    binding.tvMessage.text = state.actionSuccessMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoAtivo))
                    binding.tvMessage.isVisible = true
                } else if (showEmptyMessage) {
                    // MENSAGEM DE LISTA VAZIA (Se a API retornar [], isto é o que aparece)
                    binding.tvMessage.text = "Não existem entregas agendadas ou passadas. Clique no '+' para agendar uma."
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
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