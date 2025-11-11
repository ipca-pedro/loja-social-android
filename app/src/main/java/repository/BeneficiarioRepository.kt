package repository

import com.example.loja_social.api.ApiService
import com.example.loja_social.api.BeneficiariosResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BeneficiarioRepository(private val apiService: ApiService) {

    suspend fun getBeneficiarios(): BeneficiariosResponse {
        return withContext(Dispatchers.IO) {
            apiService.getBeneficiarios()
        }
    }
}