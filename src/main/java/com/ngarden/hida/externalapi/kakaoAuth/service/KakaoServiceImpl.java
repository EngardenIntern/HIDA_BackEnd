package com.ngarden.hida.externalapi.kakaoAuth.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ngarden.hida.domain.user.dto.request.UserCreateRequest;
import com.ngarden.hida.domain.user.dto.response.UserCreateResponse;
import com.ngarden.hida.domain.user.entity.UserEntity;
import com.ngarden.hida.domain.user.repository.UserRepository;
import com.ngarden.hida.domain.user.service.UserService;
import com.ngarden.hida.domain.user.service.UserServiceImpl;
import com.ngarden.hida.externalapi.kakaoAuth.dto.response.AuthLoginResponse;
import com.ngarden.hida.externalapi.kakaoAuth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoServiceImpl implements KakaoService{

    private final UserServiceImpl userServiceImpl;
    @Value("${KAKAO.CLIENT-ID}")
    private String client_id;

    @Value("${KAKAO.REDIRECT-URI}")
    private String redirect_uri;

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public ResponseEntity<AuthLoginResponse> login(String code) throws IOException{
        return getKakaoUserIdByKakaoAccessToken(getKakaoAccessToken(code));
    }

    public ResponseEntity<AuthLoginResponse> login(Authentication authentication) {
        UserEntity user = userRepository.findByOuthId(Long.valueOf(authentication.getName()));

        if(user == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        String accessToken = jwtTokenProvider.createAccessToken(user.getOuthId());
        return new ResponseEntity<>(new AuthLoginResponse(accessToken, user.getRefreshToken()), HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> logout(Authentication authentication){
        UserEntity user = userRepository.findByOuthId(Long.valueOf(authentication.getName()));

        if(user == null)
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        user.setRefreshToken(null);
        userRepository.save(user);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    //로그인한 사용자 정보 가져오기
    public ResponseEntity<UserCreateResponse> getUserCreateResponse(Authentication authentication) {
        UserEntity user = userRepository.findByOuthId(Long.valueOf(authentication.getName()));

        if(user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(new UserCreateResponse(user.getUserId(), user.getUserName(),user.getEmail(), user.getOuthId()), HttpStatus.OK);
    }

    private ResponseEntity<AuthLoginResponse> getKakaoUserIdByKakaoAccessToken(String kakaoAccessToken) throws IOException {
        JsonElement element = getJsonElementByAccessToken(kakaoAccessToken);

        Long outhId = element.getAsJsonObject().get("id").getAsLong();
        JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
        JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();
        String nickname = properties.getAsJsonObject().get("nickname").getAsString();
        String email = kakao_account.getAsJsonObject().get("email").getAsString();

        System.out.println("outhId = " + outhId);
        UserEntity user = userRepository.findByOuthId(outhId);

        if(user == null)
            return register(outhId,nickname,email);

        String accessToken = jwtTokenProvider.createAccessToken(outhId);
        String refreshToken = jwtTokenProvider.createRefreshToken(outhId);

        user.setRefreshToken(refreshToken);

        userRepository.save(user);

        return new ResponseEntity<>(new AuthLoginResponse(accessToken, refreshToken), HttpStatus.OK);
    }

    private ResponseEntity<AuthLoginResponse> register(Long outhId, String nickname, String email){

        String accessToken = jwtTokenProvider.createAccessToken(outhId);
        String refreshToken = jwtTokenProvider.createRefreshToken(outhId);

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email(email)
                .userName(nickname)
                .refreshToken(refreshToken)
                .outhId(outhId)
                .build();
        userService.createUser(userCreateRequest);

        return new ResponseEntity<>(new AuthLoginResponse(accessToken, refreshToken), HttpStatus.CREATED);
    }

    private JsonElement getJsonElementByAccessToken(String token) throws IOException {
        String reqUrl = "https://kapi.kakao.com/v2/user/me";

        URL url = new URL(reqUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);

        return getJsonElement(httpURLConnection);
    }

    public String getKakaoAccessToken (String authorize_code) throws UnsupportedEncodingException {
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";


        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id="+client_id);
            sb.append("&redirect_uri="+redirect_uri);
            sb.append("&code=" + authorize_code);
            bw.write(sb.toString());
            bw.flush();

            int responseCode = conn.getResponseCode();
            log.info("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));


            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            log.info("response body : " + result);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            access_Token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

            log.info("access_token : " + access_Token);
            log.info("refresh_token : " + refresh_Token);

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return access_Token;
    }

    private JsonElement getJsonElement(HttpURLConnection httpURLConnection) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String line;
        StringBuilder result = new StringBuilder();

        while((line = bufferedReader.readLine()) != null){
            result.append(line);
        }

        bufferedReader.close();

        return JsonParser.parseString(result.toString());
    }

    //로그인한 사용자 정보 가져오기
    public ResponseEntity<UserCreateResponse> getUserInfo(Authentication authentication) {
        UserEntity user = userRepository.findByOuthId(Long.valueOf(authentication.getName()));

        if(user == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(new UserCreateResponse(user.getUserId(), user.getUserName(),user.getEmail(), user.getOuthId()), HttpStatus.OK);
    }

    // 로그인한 사용자의 ID를 반환
    public Long getLoggedInUserId(Authentication authentication) {
        ResponseEntity<UserCreateResponse> responseEntity = getUserInfo(authentication);

        if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
            throw new IllegalStateException("사용자 정보를 가져올 수 없습니다.");
        }

        return responseEntity.getBody().getUserId();
    }
}
