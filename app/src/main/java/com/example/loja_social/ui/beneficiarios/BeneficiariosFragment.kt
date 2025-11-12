package com.example.loja_social.ui.beneficiarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentBeneficiariosBinding
import com.example.loja_social.repository.BeneficiarioRepository
import kotlinx.coroutines.launch

class BeneficiariosFragment : Fragment() {

    private var _binding: FragmentBeneficiariosBinding? = null
    private val binding get() = _binding!!

    // VIEWMODEL ATUALIZADO (agora usa o ViewModel local)
    private val viewModel: BeneficiariosViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = BeneficiarioRepository(apiService)
        // FACTORY ATUALIZADA
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

        // Observar o estado de loading
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading // <-- Erro 'isLoading' corrigido
                binding.tvResultadosBeneficiarios.isVisible = !isLoading
            }
        }

        // Observar a lista de beneficiários
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { beneficiarios ->
                // O uiState agora é uma List<Beneficiario>, por isso o 'joinToString' funciona
                if (beneficiarios.isNotEmpty()) {
                    val nomes = beneficiarios.joinToString(separator = "\n") { // <-- Erro 'joinToString' corrigido
                        "- ${it.nomeCompleto} (${it.estado})" // <-- Erro 'it' corrigido
                    }
                    binding.tvResultadosBeneficiarios.text = nomes
                    binding.tvErro.isVisible = false
                }
            }
        }

        // Observar mensagens de erro
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMsg -> // <-- Erro 'errorMessage' corrigido
                binding.tvErro.text = errorMsg
                binding.tvErro.isVisible = (errorMsg != null)
                if (errorMsg != null) {
                    binding.tvResultadosBeneficiarios.text = "" // Limpar dados antigos se houver erro
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}