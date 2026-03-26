package com.ct240.backend.service;

import com.ct240.backend.dto.response.SseResponse;
import com.ct240.backend.entity.User;
import com.ct240.backend.enums.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterService {

    //trạm phát sóng ->
    // người dùng phải đăng ký tham gia vô trạm này

    // Lưu emitter của từng user theo userId
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Autowired
    PermissionService permissionService;

    public SseEmitter createEmitter(Authentication authentication) {
        String  userId = (String)authentication.getDetails();

        /// tham số truyền vào SseEmitter là timeout nên truyền MAX_VALUE có nghĩa là vĩnh viễn
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // giữ kết nối lâu dài

        emitters.put(userId, emitter);

        // Khi client disconnect thì xóa khỏi map
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        return emitter;
    }

    ///đẩy dữ liệu xuống người dùng
    public void sendToUser(String userId, Object data) {
        SseEmitter emitter = emitters.get(userId); // kiểm tra user đó có đang giữ kết nối hay không
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(data)); // truyền cái object data vô
            } catch (Exception e) {
                emitters.remove(userId); // gửi lỗi thì xóa luôn
            }
        }
    }

    public void sendToUsers(List<String> userIds, SseResponse response){
        for (String userId : userIds) {
            sendToUser(userId, response);
        }
    }


    ///event listener sẽ gọi hàm này
    public void processSpaceUpdate(String spaceId, Type type, List<String> userIds) {
        SseResponse sseResponse = new SseResponse();
        sseResponse.setType(type);
        sendToUsers(userIds, sseResponse);
    }

}
