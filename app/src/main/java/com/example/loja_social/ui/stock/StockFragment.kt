package com.example.loja_social.ui.stock

import android.app.DatePickerDialog
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
import androidx.navigation.fragment.findNavController
import com.example.loja_social.R
import com.example.loja_social.api.Produto
import com.example.loja_social.api.RetrofitInstance
import com.example.loja_social.databinding.FragmentStockBinding
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Fragment para adicionar novo stock.
 * Permite selecionar categoria e produto, definir quantidade e data de validade.
 * Inclui validação e feedback visual de sucesso/erro.
 */
class StockFragment : Fragment() {

    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    /** Produto selecionado pelo utilizador */
    private var selectedProduto: Produto? = null
    /** Lista completa de produtos (para filtragem por categoria) */
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

        // Botão para ver lista de stock
        binding.btnViewStock.setOnClickListener {
            findNavController().navigate(R.id.action_nav_stock_to_nav_stock_list)
        }
    }

    /**
     * Configura os listeners dos componentes da UI.
     * - Botão de adicionar: valida e envia os dados
     * - Dropdowns: categoria e produto (com dependência entre eles)
     * - Campo de data: abre DatePicker
     */
    private fun setupListeners() {

        // Listener para o botão de adicionar stock
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

        // Listener para o campo de data - abre DatePicker
        binding.etDataValidade.setOnClickListener {
            showDatePicker()
        }

        // Listener para o ícone de calendário
        binding.tilDataValidade.setEndIconOnClickListener {
            showDatePicker()
        }
    }

    /**
     * Mostra um DatePickerDialog para selecionar a data de validade.
     * A data mínima é hoje (não permite datas passadas).
     * Formata a data selecionada como DD/MM/AAAA.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Formata a data como DD/MM/AAAA (formato do utilizador)
                val formattedDate = String.format(
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1, // Month é 0-indexed no Calendar
                    selectedYear
                )
                binding.etDataValidade.setText(formattedDate)
            },
            year,
            month,
            day
        )

        // Define data mínima como hoje (não permite selecionar datas passadas)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
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
                    binding.cardSuccess.isVisible = false
                    binding.cardError.isVisible = true
                    binding.tvErrorMessage.text = state.errorMessage
                    // Reativa o botão (verifica se há produto selecionado E se não está a carregar o formulário)
                    binding.btnAddStock.isEnabled = selectedProduto != null && !state.isFormLoading
                }

                // 3. Sucesso
                else if (state.successMessage != null) {
                    binding.cardError.isVisible = false
                    binding.cardSuccess.isVisible = true
                    binding.tvSuccessMessage.text = state.successMessage

                    // Limpar formulário após sucesso
                    binding.etQuantidade.setText("")
                    binding.etDataValidade.setText("")
                    selectedProduto = null
                    binding.actvProduto.setText("")
                    binding.actvCategoria.setText("")
                    binding.btnAddStock.isEnabled = false
                } else {
                    // Sem mensagens
                    binding.cardSuccess.isVisible = false
                    binding.cardError.isVisible = false
                }

                // 4. Carregamento inicial de dados
                if (!state.isLoading && allProducts.isEmpty()) {
                    allProducts = state.produtos // Guarda todos os produtos
                    updateCategorySpinner(state.categorias.map { it.nome }) // Popula o primeiro spinner
                }
            }
        }
    }

    /**
     * Popula o dropdown de categorias.
     * Configura listeners para garantir que o dropdown abre corretamente.
     * @param categoryNames Lista de nomes de categorias
     */
    private fun updateCategorySpinner(categoryNames: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categoryNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        }
        binding.actvCategoria.setAdapter(adapter)
        // Garantir que o dropdown abre ao clicar
        binding.actvCategoria.setOnClickListener {
            binding.actvCategoria.showDropDown()
        }
        binding.actvCategoria.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.actvCategoria.showDropDown()
            }
        }
    }

    /**
     * Popula o dropdown de produtos (filtrados pela categoria selecionada).
     * Configura listeners para garantir que o dropdown abre corretamente.
     * @param products Lista de produtos a exibir (já filtrados por categoria)
     */
    private fun updateProductSpinner(products: List<Produto>) {
        val productNames = products.map { it.nome }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            productNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        }
        binding.actvProduto.setAdapter(adapter)
        // Garantir que o dropdown abre ao clicar
        binding.actvProduto.setOnClickListener {
            if (productNames.isNotEmpty()) {
                binding.actvProduto.showDropDown()
            }
        }
        binding.actvProduto.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && productNames.isNotEmpty()) {
                binding.actvProduto.showDropDown()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}