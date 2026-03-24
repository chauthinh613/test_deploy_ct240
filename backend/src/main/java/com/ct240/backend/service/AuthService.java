package com.ct240.backend.service;

import com.ct240.backend.dto.request.AuthRequest;
import com.ct240.backend.dto.request.IntrospectRequest;
import com.ct240.backend.dto.request.UserCreationRequest;
import com.ct240.backend.dto.response.AuthResponse;
import com.ct240.backend.dto.response.IntrospectResponse;
import com.ct240.backend.dto.response.UserResponse;
import com.ct240.backend.entity.User;
import com.ct240.backend.exception.AppException;
import com.ct240.backend.exception.ErrorCode;
import com.ct240.backend.mapper.UserMapper;
import com.ct240.backend.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserMapper userMapper;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public UserResponse register(UserCreationRequest request){
        if(userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    public AuthResponse login(AuthRequest request){
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);


        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .authenticated(authenticated)
                .username(request.getUsername())
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            var token = request.getToken();

            JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());

            SignedJWT signedJWT = SignedJWT.parse(token);

            // kiểm tra token có hết hạn hay không
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            boolean verify = signedJWT.verify(jwsVerifier);

            return IntrospectResponse.builder()
                    //nếu token hợp lệ và thời gian hết han sau hiện tại thì ok
                    .valid(verify && expiryTime.after(new Date()))
                    .build();
        }
        catch (Exception e){
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }

    private String generateToken(String username){
        String userId = userRepository.findIdByUsername(username).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                //sub thường là username
                .subject(username)
                .claim("userId", userId)
                //thời gian đăng nhập vô
                .issueTime(new Date())
                //hết hạn trong 1 giờ
                .expirationTime(new Date(System.currentTimeMillis() + 3 * 60 * 60 * 1000))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {

            throw new RuntimeException(e);
        }
    }

    public void logout(){}

}
