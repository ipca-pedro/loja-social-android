    private fun setupListeners() {
        // Listener para a seleção do Beneficiário
        binding.actvBeneficiario.setOnItemClickListener { parent, _, position, _ ->
            viewModel.clearMessages()
            val selectedItem = parent.getItemAtPosition(position).toString()
            selectedBeneficiarioId = viewModel.uiState.value.beneficiarios.find {
                "${it.nomeCompleto} (${it.numEstudante ?: "N/A"})" == selectedItem
            }?.id
            // Ativa o botão após a seleção
            binding.btnAgendar.isEnabled = selectedBeneficiarioId != null
        }

        // Listener para o botão de agendamento
        binding.btnAgendar.setOnClickListener {
            val nomeCompleto = binding.actvBeneficiario.text.toString().trim()
            val dataAgendamento = binding.etDataAgendamento.text.toString().trim()

            if (selectedBeneficiarioId != null && dataAgendamento.isNotEmpty()) {
                // Mantido: chamada atual do ViewModel para não quebrar a compilação
                viewModel.agendarEntrega(nomeCompleto, dataAgendamento)
            } else {
                Toast.makeText(context, "Selecione um beneficiário e uma data.", Toast.LENGTH_SHORT).show()
            }
        }
    }
