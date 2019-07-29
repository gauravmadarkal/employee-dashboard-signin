package com.betsol.employeePortal.service;


import org.apache.log4j.Logger;
import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;

import com.betsol.employeePortal.model.User;
import com.betsol.employeePortal.repository.UserRepo;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {
    final static Logger logger = Logger.getLogger(AuthService.class);

    @Value("${microsoft.auth.code.url}")
    private String authcodeURL;

    @Value("${microsoft.auth.accessCode.url}")
    private String accessCodeUrl;

    @Value("${microsoft.auth.profilePhoto.url}")
    private String profilePhotoUrl;

    @Value("${microsoft.client.id}")
    private  String appId;

    @Value("${microsoft.client.key}")
    private  String appSecret;

    @Value("${microsoft.auth.url}")
    private  String authUrl;

    @Value("${microsoft.auth.redirect.url}")
    private String redirectUrl;

    @Value("${microsoft.auth.accessCode.managerUrl}")
    private String managerAccessCodeUrl;


    final Cacheonix cacheManager = Cacheonix.getInstance();
    final Cache<String, String> cache = cacheManager.getCache("auth_details");

    @Autowired
    UserRepo userRepo;

    @Autowired
    User newuser;

    @Autowired
    RestTemplate restTemplate;


    public void loginService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("User Clicked on login");
        String state = "wdjdfnsdvjnsdvj";
        String scope = "https://graph.microsoft.com/user.read+offline_access+profile+openid";
        String finalUrl = String.format(authUrl, appId, scope, state, redirectUrl);
        response.sendRedirect(finalUrl);
    }
    public ResponseEntity<HashMap<String, Object>> onRedirection(HttpServletRequest request, HttpServletResponse response)  {
        HttpHeaders responseHeaders = new HttpHeaders();
        HashMap<String,Object> map = new HashMap<>();
        logger.info("User redirected after login");
        final String code = request.getParameter("code");
        if(code==null){
            logger.error("Code not present, or not sent by Microsoft");
            return ResponseEntity.badRequest().body(null);
        }
        else {
            String auth_code = getAuthCode(code, redirectUrl, appId, appSecret);
            JSONObject jsonObject = new JSONObject(auth_code);
            String accessTokenObject = getAccessToken(jsonObject.getString("access_token"));
            System.out.println(accessTokenObject);
            String email = new JSONObject(accessTokenObject).get("mail").toString();
            String access_token = jsonObject.getString("access_token");
            System.out.println(access_token);
            System.out.println(email);
            String username = new JSONObject(accessTokenObject).get("displayName").toString();
            User user = userRepo.findByEmailId(email);
            if (user != null) {
                logger.info("Existing User logged in");
                cache.put(access_token, String.valueOf(user.isAdmin()));
                map.put("userId", user.getUserId());
                map.put("isAdmin", user.isAdmin());
                map.put("userName", user.getUserName());
                responseHeaders.add("Authorization", "Bearer " + access_token);
                return ResponseEntity.ok().headers(responseHeaders).body(map);
            }
            logger.info("New User Logged in, adding to DB");
            newuser.setUserId(UUID.randomUUID().toString());
            newuser.setUserName(username);
            newuser.setEmail(email);
            try{
                userRepo.save(newuser);
            }catch (Exception e){
                logger.error("user failed to get added to DB");
            }
            cache.put(access_token, String.valueOf(newuser.isAdmin()));
            map.put("isAdmin", newuser.isAdmin());
            map.put("userName", newuser.getUserName());
            responseHeaders.add("Authorization", "Bearer " + access_token);
            return ResponseEntity.ok().headers(responseHeaders).body(map);
        }
    }

    public String getAuthCode(String code, String redirectURL, String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", redirectURL);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(authcodeURL, request, String.class);
            return response.getBody();
    }

    public  String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + code);
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(accessCodeUrl, HttpMethod.GET, request, String.class);
        return responseEntity.getBody();
    }

    public Boolean validateToken(String authorization,String userid) {
        if (authorization != null || authorization.startsWith("Bearer ")) {
            logger.info("Validate Token called");
            String token = authorization.substring("Bearer " .length());
            if(cache.containsKey(token)){
                logger.info("User validated successfully");
                return Boolean.valueOf(cache.get(token));
            }
        }
        return null;
    }


    public boolean inValidate(String authorization) {
        logger.info("User clicked on logout");
        try{
            cache.remove(authorization);
        }catch (Exception e){
            logger.error("Failed to remove user, after logout");
        }
        return true;
    }


    @Bean
    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    public void printAll() {
        System.out.println(cache.entrySet());
    }
}
