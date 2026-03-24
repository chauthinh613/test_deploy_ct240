package com.ct240.backend.service;

import com.ct240.backend.dto.request.ChangePasswordRequest;
import com.ct240.backend.dto.request.UserCreationRequest;
import com.ct240.backend.dto.request.UserUpdateRequest;
import com.ct240.backend.dto.response.UserResponse;
import com.ct240.backend.entity.User;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.UserMapper;
import com.ct240.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserMapper userMapper;

    @Autowired
    PermissionService permissionService;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);;

    public UserResponse createUser(UserCreationRequest request){
        if(userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }


    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse getUserByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(UserUpdateRequest request, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        userMapper.updateUser(user, request);

        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .avatarURL(user.getAvatarURL())
                .build();
    }

    public List<UserResponse> searchUsers(String keyword){
        var listUsers = userRepository.findByUsernameContaining(keyword);

        return listUsers.stream()
                .map(user -> userMapper.toUserResponse(user))
                .collect(Collectors.toList());
    }

    public UserResponse getUser(String userId){
        User user = userRepository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        return userMapper.toUserResponse(user);
    }

    public void updatePassword(ChangePasswordRequest request, Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        //kiểm tra mật khẩu đang nhập có đúng hay không thì mới đổi được
        String currentPassword = request.getCurrentPassword();

        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

//    user upload anh → luu theo userId.png
//    upload lan sau → ghi de (khong tao file moi)
//    khong xoa default
//    khong xoa nham file vua upload
    @Transactional
    public UserResponse uploadAvatar(MultipartFile file, Authentication authentication) {

        // lay user dang dang nhap
        User user = permissionService.getUserAuth(authentication);

        // kiem tra file rong
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        // kiem tra dinh dang file
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/png")
                        && !contentType.equals("image/jpeg")
                        && !contentType.equals("image/jpg"))) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // kiem tra kich thuoc file (toi da 2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        try {
            // lay ten file goc
            String originalName = file.getOriginalFilename();

            // kiem tra ten file hop le
            if (originalName == null || !originalName.contains(".")) {
                throw new AppException(ErrorCode.INVALID_FILE_TYPE);
            }

            // lay duoi file (.png, .jpg)
            String extension = originalName.substring(originalName.lastIndexOf("."));

            // dat ten file theo userId de tranh tao nhieu file
            String fileName = user.getId() + extension;

            // lay duong dan thu muc goc cua project
            String baseDir = new File(System.getProperty("user.dir")).getParent();
            String uploadDir = baseDir + "/uploads/avatars/";

            // neu chua co thu muc thi tao moi
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // tao file dich de luu
            File dest = new File(uploadDir + fileName);

            // luu file vao he thong (se ghi de neu da ton tai)
            file.transferTo(dest);

            // duong dan avatar mac dinh
//            String DEFAULT_AVATAR = "/uploads/avatars/default.png";

            // lay avatar hien tai cua user
            String currentAvatar = user.getAvatarURL();

            // duong dan avatar moi
            String newAvatarUrl = "/uploads/avatars/" + fileName;

            // chi xoa file cu neu:
            // - khong phai avatar mac dinh
            // - va khong trung voi file moi (tranh xoa nham file vua upload)
            if(currentAvatar != null && !currentAvatar.equals(newAvatarUrl)) {

                // chuyen url -> duong dan vat ly
                String relativePath = currentAvatar.replace("/uploads/", "");
                String oldPath = baseDir + "/uploads/" + relativePath;

                File oldFile = new File(oldPath);

                // neu file ton tai thi xoa
                if (oldFile.exists()) oldFile.delete();
            }

            // cap nhat avatar moi vao database
            user.setAvatarURL(newAvatarUrl);
            userRepository.save(user);

            // tra ve thong tin user
            return userMapper.toUserResponse(user);

        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }

    @Transactional
    public UserResponse deleteAvatar(Authentication authentication){
        User user = permissionService.getUserAuth(authentication);

        String baseDir = new File(System.getProperty("user.dir")).getParent();

        String avatarUrl = user.getAvatarURL();
        String DEFAULT_AVATAR = null;

//        if (DEFAULT_AVATAR.equals(avatarUrl)){
//            throw new AppException(ErrorCode.CANNOT_DELETE_DEFAULT_AVATAR);
//        }

        String oldPath = baseDir + avatarUrl;
        File oldFile = new File(oldPath);

        user.setAvatarURL(DEFAULT_AVATAR);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }


}
