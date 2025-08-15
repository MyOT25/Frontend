// ChatRoomApi.kt
import retrofit2.Call
import retrofit2.http.GET

interface ChatRoomApi {
    // 채팅방 목록 조회 API
    @GET("/api/chatrooms")
    fun getChatRooms(): Call<ChatRoomListResponse>
}
