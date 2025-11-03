# 🛍️ Integração API Loja Social (Kotlin)

Este documento descreve como integrar a **API da Loja Social** num cliente **Kotlin/Android**, utilizando **Retrofit** e **data classes** para representar os modelos de dados.

---

## 🌍 1. Configuração Base

O cliente Retrofit deve ser inicializado com o URL base do servidor.

```kotlin
const val BASE_URL = "https://url-da-sua-api.com/"
```

## 🔐 2. Autenticação

A API utiliza autenticação por Token (JWT) para rotas administrativas.

### 🔄 Fluxo de Autenticação

1. O utilizador faz POST para `/api/auth/login` com email e password.
2. A API retorna um Token.
3. O Token deve ser guardado (ex: DataStore ou SharedPreferences).
4. Para chamadas administrativas, o header deve incluir:

```
Authorization: Bearer <seu_token>
```

### 🧩 Modelos de Autenticação

```kotlin
// Objeto enviado no body do POST /api/auth/login
data class LoginRequest(
    val email: String,
    val password: String
)

// Resposta esperada do /api/auth/login
data class LoginResponse(
    val token: String
    // Pode incluir outros dados: nome, role, etc.
)
```

## 📦 3. Modelos de Dados (Data Classes)

Os nomes dos campos devem corresponder exatamente ao JSON da API.
Se diferirem, use `@SerializedName("nome_no_json")`.

### 🗓️ Campanhas

```kotlin
/**
 * GET /api/public/campanhas
 */
data class Campanha(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val data_inicio: String,
    val data_fim: String,
    val ativa: Boolean
)
```

### 📊 Resumo de Stock

```kotlin
/**
 * GET /api/public/stock-summary
 */
data class StockSummaryItem(
    val categoria: String,
    val status: String, // Ex: "Disponível", "Baixo"
    val percentagem: Double? // Exemplo
)
```

### 👥 Beneficiários

```kotlin
/**
 * GET /api/beneficiarios (Admin)
 */
data class Beneficiario(
    val id: Int,
    val nome: String,
    val email: String,
    val numero_aluno: String?
)
```

### ➕ Adicionar Stock

```kotlin
/**
 * POST /api/stock (Admin)
 */
data class AddStockRequest(
    val nome_produto: String,
    val quantidade: Int,
    val categoria_id: Int
)
```

### 📩 Formulário de Contacto

```kotlin
/**
 * POST /api/public/contacto
 */
data class ContactoRequest(
    val nome: String,
    val email: String,
    val mensagem: String
)
```

## 🚀 4. Definição da API (Retrofit)

```kotlin
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Rotas Públicas ---

    @GET("api/public/campanhas")
    suspend fun getCampanhas(): List<Campanha>

    @GET("api/public/stock-summary")
    suspend fun getStockSummary(): List<StockSummaryItem>

    @POST("api/public/contacto")
    suspend fun enviarFormularioContacto(
        @Body request: ContactoRequest
    ): Response<Unit>

    // --- Autenticação ---

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // --- Rotas Administrativas (Protegidas) ---

    @GET("api/beneficiarios")
    suspend fun getBeneficiarios(
        @Header("Authorization") authToken: String
    ): List<Beneficiario>

    @POST("api/stock")
    suspend fun addStockItem(
        @Header("Authorization") authToken: String,
        @Body request: AddStockRequest
    ): Response<Unit>

    @PUT("api/entregas/{id}/concluir")
    suspend fun concluirEntrega(
        @Header("Authorization") authToken: String,
        @Path("id") entregaId: Int
    ): Response<Unit>

    // --- Utilitários ---

    @GET("health")
    suspend fun checkHealth(): Response<Unit>
}
```

## 💡 Boa Prática: Interceptor de Autenticação

Para evitar repetir o header Authorization em todas as funções,
crie um Interceptor no OkHttpClient que o adiciona automaticamente.

```kotlin
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
```

Depois, adicione o interceptor ao Retrofit:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor { getTokenFromStorage() })
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

Assim, o seu ApiService fica mais limpo:

```kotlin
@GET("api/beneficiarios")
suspend fun getBeneficiarios(): List<Beneficiario>
```

## 🧭 Resumo:

- Use **Retrofit + OkHttp** para comunicação HTTP.
- Guarde o token em **DataStore/SharedPreferences**.
- Use um **Interceptor** para adicionar o header de autenticação.
- Crie **data classes** que correspondam aos modelos JSON da API.
