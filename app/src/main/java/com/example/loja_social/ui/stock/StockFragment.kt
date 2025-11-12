package com.example.loja_social.ui.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.loja_social.R
import com.example.loja_social.api.Produto
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentStockBinding
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StockFragment : Fragment() {

    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    // Variáveis de estado da UI
    private var selectedProduto: Produto? = null
    private var allProducts: List<Produto> = emptyList()

    private val viewModel: StockViewModel by viewModels {
        val apiService = RetrofitInstance.api
        val repository = StockRepository(apiService)
        StockViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()

        // O botão começa desativado até ser selecionado um produto
        binding.btnAddStock.isEnabled = false
    }

    private fun setupListeners() {

        // Listener para o botão de submissão
        binding.btnAddStock.setOnClickListener {
            val produto = selectedProduto
            val quantidade = binding.etQuantidade.text.toString()
            val dataValidade = binding.etDataValidade.text.toString()

            if (produto != null) {
                viewModel.addStockItem(produto.id, quantidade, dataValidade)
            } else {
                viewModel.clearMessages()
                Toast.makeText(context, "Por favor, selecione um produto.", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para a seleção de Categoria
        binding.actvCategoria.setOnItemClickListener { parent, _, position, _ ->
            viewModel.clearMessages()
            val selectedCategoryName = parent.getItemAtPosition(position).toString()

            // 1. Filtra os produtos que pertencem à categoria selecionada
            val filteredProducts = allProducts.filter { it.categoria == selectedCategoryName }
            updateProductSpinner(filteredProducts)

            // 2. Limpa a seleção de produto anterior e desativa o botão
            selectedProduto = null
            binding.actvProduto.setText("")
            binding.btnAddStock.isEnabled = false
        }

        // Listener para a seleção de Produto
        binding.actvProduto.setOnItemClickListener { parent, _, position, _ ->
            viewModel.clearMessages()
            val selectedProductName = parent.getItemAtPosition(position).toString()
            // Encontra o objeto Produto completo (inclui o ID)
            selectedProduto = allProducts.find { it.nome == selectedProductName }
            // Ativa o botão
            binding.btnAddStock.isEnabled = selectedProduto != null
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->

                // 1. Loading
                // CORRIGIDO: Acessar progressBarOverlay (nome gerado pelo Binding)
                binding.progressBarOverlay.isVisible = state.isLoading || state.isFormLoading

                // Desativa o botão se estiver a carregar o formulário
                binding.btnAddStock.text = if (state.isFormLoading) "A Adicionar..." else "Adicionar Stock"

                // 2. Erro
                if (state.errorMessage != null) {
                    binding.tvMessage.text = state.errorMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoInativo))
                    binding.tvMessage.isVisible = true
                    // Reativa o botão (verifica se há produto selecionado E se não está a carregar o formulário)
                    binding.btnAddStock.isEnabled = selectedProduto != null && !state.isFormLoading
                }

                // 3. Sucesso
                else if (state.successMessage != null) {
                    binding.tvMessage.text = state.successMessage
                    binding.tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.estadoAtivo))
                    binding.tvMessage.isVisible = true

                    // Limpar formulário após sucesso
                    binding.etQuantidade.setText("")
                    binding.etDataValidade.setText("")
                }

                // 4. Carregamento inicial de dados
                if (!state.isLoading && allProducts.isEmpty()) {
                    allProducts = state.produtos // Guarda todos os produtos
                    updateCategorySpinner(state.categorias.map { it.nome }) // Popula o primeiro spinner
                }
            }
        }
    }

    // Função para preencher o Spinner de Categorias
    private fun updateCategorySpinner(categoryNames: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.actvCategoria.setAdapter(adapter)
    }

    // Função para preencher o Spinner de Produtos
    private fun updateProductSpinner(products: List<Produto>) {
        val productNames = products.map { it.nome }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, productNames)
        binding.actvProduto.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}