package com.example.loja_social.ui.beneficiarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentBeneficiariosBinding
import com.example.loja_social.ui.main.MainViewModel
import com.example.loja_social.ui.main.MainViewModelFactory
import kotlinx.coroutines.launch
import repository.BeneficiarioRepository 

class BeneficiariosFragment : Fragment() {

    private var _binding: FragmentBeneficiariosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = BeneficiarioRepository(apiService) // ✅ CORRIGIDO
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