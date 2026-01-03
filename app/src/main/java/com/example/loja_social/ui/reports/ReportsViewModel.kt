package com.example.loja_social.ui.reports

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.loja_social.api.Beneficiario
import com.example.loja_social.api.StockItem
import com.example.loja_social.api.StockItemData 
import com.example.loja_social.repository.BeneficiarioRepository
import com.example.loja_social.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ReportType(val title: String) {
    VALIDADE("Validade de Stock"),
    INVENTARIO("Inventário Geral"),
    BENEFICIARIOS("Relatório de Beneficiários")
}

data class ReportsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val validadeData: List<StockItem> = emptyList(), // Reutilizando modelos existentes por enquanto
    val inventarioData: List<StockItem> = emptyList(),
    val beneficiariosData: List<Beneficiario> = emptyList()
)

class ReportsViewModel(
    private val stockRepository: StockRepository,
    private val beneficiarioRepository: BeneficiarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun generateReport(context: Context, type: ReportType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                // 1. Fetch data if needed (naive caching for now)
                when (type) {
                    ReportType.VALIDADE, ReportType.INVENTARIO -> {
                        if (_uiState.value.validadeData.isEmpty()) {
                            val response = stockRepository.getStock() // Pega tudo
                            if (response.success && response.data != null) {
                                _uiState.value = _uiState.value.copy(validadeData = response.data, inventarioData = response.data)
                            } else {
                                throw Exception(response.message ?: "Erro ao buscar stock")
                            }
                        }
                    }
                    ReportType.BENEFICIARIOS -> {
                         if (_uiState.value.beneficiariosData.isEmpty()) {
                            val response = beneficiarioRepository.getBeneficiarios()
                            if (response.success) {
                                _uiState.value = _uiState.value.copy(beneficiariosData = response.data)
                            } else {
                                throw Exception(response.message ?: "Erro ao buscar beneficiários")
                            }
                        }
                    }
                }

                // 2. Generate PDF
                val file = createPdf(context, type)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Relatório salvo em Downloads: ${file.name}"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Erro: ${e.message}")
            }
        }
    }

    private fun createPdf(context: Context, type: ReportType): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points (approx)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Configuração Texto
        paint.textSize = 12f
        var y = 40f
        val xMargin = 40f

        // Título
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Relatório: ${type.title}", xMargin, y, paint)
        y += 30f
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Gerado em: ${LocalDate.now()}", xMargin, y, paint)
        y += 40f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawLine(xMargin, y, pageInfo.pageWidth - xMargin, y, paint)
        y += 20f
        paint.style = Paint.Style.FILL

        when (type) {
            ReportType.VALIDADE -> {
                // Cabeçalho
                paint.isFakeBoldText = true
                canvas.drawText("Produto", xMargin, y, paint)
                canvas.drawText("Validade", xMargin + 200, y, paint)
                canvas.drawText("Qtd", xMargin + 350, y, paint)
                y += 20f
                paint.isFakeBoldText = false

                // Dados (Filtrar quem tem validade proxima)
                // Nota: O modelo StockItem do backend agrega por produto. 
                // Para validade exata precisaria dos Lotes, mas vamos usar o validade_proxima do agregado ordenado
                val items = _uiState.value.validadeData.sortedBy { it.validadeProxima ?: "9999-99-99" }
                
                items.forEach { item ->
                    if (y > pageInfo.pageHeight - 50) {
                         // Simples paginação: paramos na primeira página por enquanto para MVP
                         // Num mundo ideal, criaríamos nova página
                         return@forEach 
                    }
                    val validade = item.validadeProxima?.substringBefore("T") ?: "N/A"
                    canvas.drawText(item.produto.take(25), xMargin, y, paint)
                    canvas.drawText(validade, xMargin + 200, y, paint)
                    canvas.drawText(item.quantidadeTotal.toString(), xMargin + 350, y, paint)
                    y += 15f
                }
            }
            ReportType.INVENTARIO -> {
                paint.isFakeBoldText = true
                canvas.drawText("Categoria", xMargin, y, paint)
                canvas.drawText("Produto", xMargin + 100, y, paint)
                canvas.drawText("Qtd Total", xMargin + 300, y, paint)
                y += 20f
                paint.isFakeBoldText = false
                
                val items = _uiState.value.inventarioData.sortedBy { it.categoria }
                
                items.forEach { item ->
                     if (y > pageInfo.pageHeight - 50) return@forEach
                    canvas.drawText((item.categoria ?: "Eco").take(15), xMargin, y, paint)
                    canvas.drawText(item.produto.take(25), xMargin + 100, y, paint)
                    canvas.drawText(item.quantidadeTotal.toString(), xMargin + 300, y, paint)
                    y += 15f
                }
            }
            ReportType.BENEFICIARIOS -> {
                paint.isFakeBoldText = true
                canvas.drawText("Nome", xMargin, y, paint)
                canvas.drawText("Estado", xMargin + 200, y, paint)
                canvas.drawText("Nº Est.", xMargin + 300, y, paint)
                y += 20f
                paint.isFakeBoldText = false
                
                val items = _uiState.value.beneficiariosData.sortedBy { it.nomeCompleto }
                
                items.forEach { item ->
                    if (y > pageInfo.pageHeight - 50) return@forEach
                    canvas.drawText(item.nomeCompleto.take(25), xMargin, y, paint)
                    canvas.drawText(item.estado, xMargin + 200, y, paint)
                    canvas.drawText(item.numEstudante ?: "-", xMargin + 300, y, paint)
                    y += 15f
                }
            }
        }

        pdfDocument.finishPage(page)

        // Salvar Arquivo
        val fileName = "loja_social_${type.name.lowercase()}_${System.currentTimeMillis()}.pdf"
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } finally {
            pdfDocument.close()
        }
        
        return file
    }
}

class ReportsViewModelFactory(
    private val stockRepository: StockRepository,
    private val beneficiarioRepository: BeneficiarioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(stockRepository, beneficiarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
