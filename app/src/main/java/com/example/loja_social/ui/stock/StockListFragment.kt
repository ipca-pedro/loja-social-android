package com.example.loja_social.ui.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
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
        setupSearchAndFilters()
        observeViewModel()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupSearchAndFilters() {
        // ... (código da pesquisa e dos chips permanece o mesmo)

        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilterType(null)
        }
        binding.chipValidadeProxima.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilterType("validade_proxima")
        }
        binding.chipStockBaixo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setFilterType("stock_baixo")
        }
    }

    private fun setupCategoryFilter(categories: List<String>) {
        val allCategories = listOf("Todas as categorias") + categories
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allCategories)
        binding.actvCategoryFilter.setAdapter(adapter)

        binding.actvCategoryFilter.setOnItemClickListener { parent, _, position, _ ->
            val selectedCategory = parent.getItemAtPosition(position) as String
            if (selectedCategory == "Todas as categorias") {
                viewModel.setCategoryFilter(null)
            } else {
                viewModel.setCategoryFilter(selectedCategory)
            }
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading && !binding.swipeRefresh.isRefreshing
                binding.swipeRefresh.isRefreshing = isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                stockAdapter.submitList(state.stockItems)

                // Popular o dropdown de categorias quando os dados chegarem
                if (state.categories.isNotEmpty()) {
                    setupCategoryFilter(state.categories)
                }
                updateVisibility()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMsg ->
                if (errorMsg != null) {
                    binding.cardError.isVisible = true
                    binding.tvError.text = errorMsg
                    binding.rvStock.isVisible = false
                    binding.emptyState.isVisible = false
                } else {
                    binding.cardError.isVisible = false
                    updateVisibility()
                }
            }
        }
    }

    private fun updateVisibility() {
        val hasData = viewModel.uiState.value.stockItems.isNotEmpty()
        val hasError = viewModel.errorMessage.value != null
        val isLoading = viewModel.isLoading.value

        if (!isLoading && !hasError) {
            binding.rvStock.isVisible = hasData
            binding.emptyState.isVisible = !hasData
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
