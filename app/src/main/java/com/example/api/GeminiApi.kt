package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val geminiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

object GeminiHelper {
    suspend fun getShoppingAssistantResponse(userPrompt: String, history: List<Pair<String, String>>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Note: No active Gemini API key configured. Please configure your key in Google AI Studio to unlock smart conversations.\n\nHere is a mock assistant response: Absolutely! The gadgets in SR Shops features full warranty and next-day express delivery. Is there any particular category you are looking that I can suggest for you?"
        }

        val systemPrompt = """
            You are the premium AI Shopping Assistant of SR Shops, a luxury modern shopping platform. 
            Help the user discover products, answer questions, compare gadgets, suggest fashion sizes, and offer personalized deals.
            Be polite, elite, responsive, and mention the brand name "SR Shops". Keep your responses clean and professional, with elegant formatting.
        """.trimIndent()

        val contents = mutableListOf<Content>()
        
        // Feed history
        history.forEach { (sender, msg) ->
            val rolePart = Part(text = "$sender: $msg")
            contents.add(Content(parts = listOf(rolePart)))
        }

        // Add current user prompt
        contents.add(Content(parts = listOf(Part(text = "User: $userPrompt"))))

        try {
            val request = GenerateContentRequest(
                contents = contents,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I couldn't generate a recommendation right now. Is there any other query I can help you with?"
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error: ${e.localizedMessage ?: "Network connection error on SR Shops API."}"
        }
    }
}
