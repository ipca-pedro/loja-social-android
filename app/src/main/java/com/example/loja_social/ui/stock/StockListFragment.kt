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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loja_social.R
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.api.StockItem
import com.example.loja_social.databinding.FragmentStockListBinding
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.launch

class StockListFragment : Fragment() {

    private var _binding: FragmentStockListBinding? = null
    private val binding get() = _binding!!

    private val stockAdapter = StockAdapter { stockItem ->
        // Por enquanto, apenas mostra um toast. Depois podemos adicionar detalhes
        navigateToStockDetail(stockItem)
    }

    private val viewModel: StockListViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = StockRepository(apiService)
        StockListViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupRecyclerView() {
        binding.rvStock.apply {
            adapter = stockAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun observeViewModel() {
        // Observar loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading && !binding.swipeRefresh.isRefreshing
                binding.swipeRefresh.isRefreshing = state.isLoading

                // Atualizar lista
                stockAdapter.submitList(state.stockItems)

                // Atualizar visibilidade
                updateVisibility(state)
            }
        }
    }

    private fun updateVisibility(state: StockListUiState) {
        val hasData = state.stockItems.isNotEmpty()
        val hasError = state.errorMessage != null
        val isLoading = state.isLoading

        if (!isLoading && !hasError) {
            binding.rvStock.isVisible = hasData
            binding.emptyState.isVisible = !hasData
            binding.cardError.isVisible = false
        } else if (hasError) {
            binding.rvStock.isVisible = false
            binding.emptyState.isVisible = false
            binding.cardError.isVisible = true
            binding.tvError.text = state.errorMessage
        }
    }

    private fun navigateToStockDetail(stockItem: StockItem) {
        val bundle = Bundle().apply {
            putInt("produtoId", stockItem.produtoId)
            putString("produtoNome", stockItem.produto)
        }
        findNavController().navigate(R.id.action_nav_stock_list_to_nav_stock_detail, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

