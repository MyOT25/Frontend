// ChatRoomListRetrofitInstance.kt
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatRoomListRetrofitInstance {
    private const val BASE_URL = "https://your-api-server.com" // 서버 주소로 변경
    private const val TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjIsImxvZ2luSWQiOiJoYWt5NTAyNiIsImlhdCI6MTc1NDk5MDcwMCwiZXhwIjoxNzU1NTk1NTAwfQ.OB7JKIrVGjqG2lruN_D4q6dcbCPSP9_Hpm9cGNp5jOI"

    // Authorization 헤더를 추가하는 인터셉터
    private val authInterceptor = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $TOKEN")
            .build()
        chain.proceed(newRequest)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: ChatRoomApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatRoomApi::class.java)
    }
}
