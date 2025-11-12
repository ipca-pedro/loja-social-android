package com.example.loja_social.ui.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentStockDetailBinding
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.launch

class StockDetailFragment : Fragment() {

    private var _binding: FragmentStockDetailBinding? = null
    private val binding get() = _binding!!

    private val args: StockDetailFragmentArgs by navArgs()

    private val viewModel: StockDetailViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = StockRepository(apiService)
        StockDetailViewModelFactory(repository, args.produtoId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockDetailBinding.inflate(inflater, container, false)
        activity?.title = args.produtoNome
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading

                if (state.stockItem != null) {
                    populateDetails(state.stockItem)
                }

                if (state.errorMessage != null) {
                    binding.cardError.isVisible = true
                    binding.tvError.text = state.errorMessage
                } else {
                    binding.cardError.isVisible = false
                }
            }
        }
    }

    private fun populateDetails(stockItem: com.example.loja_social.api.StockItem) {
        binding.tvProdutoNome.text = stockItem.produto
        binding.tvCategoria.text = stockItem.categoria ?: "Sem categoria"
        binding.tvQuantidadeTotal.text = "${stockItem.quantidadeTotal} unidades"
        binding.tvNumLotes.text = "${stockItem.lotes} ${if (stockItem.lotes == 1) "lote" else "lotes"}"

        if (stockItem.validadeProxima != null) {
            binding.tvValidadeProxima.text = stockItem.validadeProxima
            binding.llValidade.isVisible = true
        } else {
            binding.llValidade.isVisible = false
        }

        // Por enquanto, mostrar mensagem de que edição/remoção requer endpoint adicional
        binding.tvInfo.text = "Para editar ou remover lotes individuais, é necessário um endpoint que retorne os lotes deste produto."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

