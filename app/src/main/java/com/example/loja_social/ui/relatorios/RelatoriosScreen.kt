package com.example.loja_social.ui.relatorios

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import com.example.loja_social.api.RelatorioEntregaItem
import com.example.loja_social.api.RelatorioStockItem
import com.example.loja_social.api.RelatorioValidadeItem
import kotlinx.coroutines.launch

@Composable
fun RelatoriosScreen(
    viewModel: RelatoriosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State to track which report is selected
    var selectedReport by remember { mutableStateOf("ENTREGAS") } // ENTREGAS, STOCK, VALIDADE
    
    // Dates for Entregas Report
    var dataInicio by remember { mutableStateOf("") }
    var dataFim by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Exportar Relatórios (PDF)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tipo de Relatório", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedReport == "ENTREGAS",
                        onClick = { selectedReport = "ENTREGAS" }
                    )
                    Text("Entregas")
                }
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedReport == "STOCK",
                        onClick = { selectedReport = "STOCK" }
                    )
                    Text("Stock vs Campanhas")
                }
                
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedReport == "VALIDADE",
                        onClick = { selectedReport = "VALIDADE" }
                    )
                    Text("Validade e Quebras")
                }
            }
        }
        
        // Inputs specific to report type
        if (selectedReport == "ENTREGAS") {
            OutlinedTextField(
                value = dataInicio,
                onValueChange = { dataInicio = it },
                label = { Text("Data Início (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("2024-01-01") }
            )
            OutlinedTextField(
                value = dataFim,
                onValueChange = { dataFim = it },
                label = { Text("Data Fim (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("2024-12-31") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                // Trigger API fetch and then PDF generation logic
                scope.launch {
                    when (selectedReport) {
                        "ENTREGAS" -> {
                            viewModel.fetchRelatorioEntregas(
                                if (dataInicio.isBlank()) null else dataInicio,
                                if (dataFim.isBlank()) null else dataFim
                            )
                        }
                        "STOCK" -> viewModel.fetchRelatorioStock()
                        "VALIDADE" -> viewModel.fetchRelatorioValidade()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Gerar Relatório")
            }
        }
        
        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        
        // Use side-effects or observation to trigger PDF generation when data arrives
        // Note: In a real app, strict One-Shot events are better, but here we can check if content is not empty
        // and trigger the PDF generation if a flag is set?
        // Let's manually trigger generation if data is present for simplicity:
    }
    
    // LaunchedEffect to watch for data changes and generate PDF if ready
    // This is a simplified approach. Ideally we'd have a stronger event system.
    LaunchedEffect(uiState.entregas, uiState.stock, uiState.validade) {
        if (selectedReport == "ENTREGAS" && uiState.entregas.isNotEmpty()) {
            generatePdfEntregas(context, uiState.entregas, dataInicio, dataFim)
        } else if (selectedReport == "STOCK" && uiState.stock.isNotEmpty()) {
            generatePdfStock(context, uiState.stock)
        } else if (selectedReport == "VALIDADE" && uiState.validade.isNotEmpty()) {
            generatePdfValidade(context, uiState.validade)
        }
    }
}

// --- PDF GENERATION LOGIC ---

fun generatePdfEntregas(context: android.content.Context, items: List<RelatorioEntregaItem>, inicio: String, fim: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    
    // Title
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 20f
    paint.color = AndroidColor.BLACK
    canvas.drawText("Relatório de Entregas", 50f, 50f, paint)
    
    paint.textSize = 14f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Período: ${if(inicio.isEmpty()) "Tudo" else inicio} a ${if(fim.isEmpty()) "Tudo" else fim}", 50f, 80f, paint)
    
    // Headers
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    var y = 120f
    canvas.drawText("Data", 50f, y, paint)
    canvas.drawText("Beneficiário", 150f, y, paint)
    canvas.drawText("Estado", 350f, y, paint)
    canvas.drawText("Colaborador", 450f, y, paint)
    
    // Items
    paint.typeface = Typeface.DEFAULT
    y += 20f
    for (item in items) {
        if (y > 800) break // Simple pagination limit (only 1 page support for MVP)
        canvas.drawText(item.dataAgendamento.take(10), 50f, y, paint)
        canvas.drawText((item.beneficiario ?: "N/A").take(25), 150f, y, paint)
        canvas.drawText(item.estado, 350f, y, paint)
        canvas.drawText((item.colaborador ?: "N/A").take(15), 450f, y, paint)
        y += 20f
    }
    
    pdfDocument.finishPage(page)
    savePdf(context, pdfDocument, "Relatorio_Entregas_${System.currentTimeMillis()}.pdf")
}

fun generatePdfStock(context: android.content.Context, items: List<RelatorioStockItem>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    
    paint.textSize = 20f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Relatório de Stock vs Campanhas", 50f, 50f, paint)
    
    paint.textSize = 12f
    var y = 100f
    
    // Headers
    canvas.drawText("Campanha", 50f, y, paint)
    canvas.drawText("Produto", 250f, y, paint)
    canvas.drawText("Total", 450f, y, paint)
    canvas.drawText("Recolhido", 500f, y, paint)
    
    y += 20f
    paint.typeface = Typeface.DEFAULT
    
    for (item in items) {
        if (y > 800) break
        canvas.drawText((item.campanhaNome).take(25), 50f, y, paint)
        canvas.drawText((item.produtoNome).take(20), 250f, y, paint)
        canvas.drawText(item.quantidadeTotal.toString(), 450f, y, paint)
        canvas.drawText(item.quantidadeRecolhida.toString(), 500f, y, paint)
        y += 20f
    }
    
    pdfDocument.finishPage(page)
    savePdf(context, pdfDocument, "Relatorio_Stock_${System.currentTimeMillis()}.pdf")
}

fun generatePdfValidade(context: android.content.Context, items: List<RelatorioValidadeItem>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    
    paint.textSize = 20f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Relatório de Validade e Quebras", 50f, 50f, paint)
    
    paint.textSize = 12f
    var y = 100f
    
    // Headers
    canvas.drawText("Produto", 50f, y, paint)
    canvas.drawText("Validade", 250f, y, paint)
    canvas.drawText("Danificado", 350f, y, paint)
    canvas.drawText("Estado", 450f, y, paint)
    
    y += 20f
    paint.typeface = Typeface.DEFAULT
    
    for (item in items) {
        if (y > 800) break
        canvas.drawText((item.produtoNome).take(25), 50f, y, paint)
        canvas.drawText(item.dataValidade?.take(10) ?: "N/A", 250f, y, paint)
        canvas.drawText(item.quantidadeDanificada.toString(), 350f, y, paint)
        
        paint.color = if (item.estadoItem == "Expirado" || item.estadoItem == "Danificado") AndroidColor.RED else AndroidColor.BLACK
        canvas.drawText(item.estadoItem, 450f, y, paint)
        paint.color = AndroidColor.BLACK
        
        y += 20f
    }
    
    pdfDocument.finishPage(page)
    savePdf(context, pdfDocument, "Relatorio_Validade_${System.currentTimeMillis()}.pdf")
}

fun savePdf(context: android.content.Context, pdfDocument: PdfDocument, filename: String) {
    try {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(path, filename)
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF guardado em Downloads: $filename", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}
