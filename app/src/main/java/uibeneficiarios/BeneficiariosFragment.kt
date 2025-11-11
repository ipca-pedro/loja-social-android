package com.example.loja_social.ui.beneficiarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.loja_social.data.api.RetrofitInstance
import com.example.loja_social.data.repository.BeneficiarioRepository
import com.example.loja_social.databinding.FragmentBeneficiariosBinding
import com.example.loja_social.ui.main.MainViewModel // Importa o ViewModel antigo
import com.example.loja_social.ui.main.MainViewModelFactory // Importa a Factory antiga
import kotlinx.coroutines.launch

/**
 * Este Fragment vai usar o ViewModel que já tinhas feito
 * (que deveríamos renomear para BeneficiariosViewModel)
 */
class BeneficiariosFragment : Fragment() {

    private var _binding: FragmentBeneficiariosBinding? = null
    private val binding get() = _binding!!

    // Usar o ViewModel que já tinhas, mas agora ligado ao Fragment
    private val viewModel: MainViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = BeneficiarioRepository(apiService)
        MainViewModelFactory(repository)
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

        // Ligar o observador do ViewModel à TextView deste Fragment
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { novoTexto ->
                binding.tvResultadosBeneficiarios.text = novoTexto
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}