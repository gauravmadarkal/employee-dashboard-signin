package com.betsol.employeePortal.service;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.cacheonix.Cacheonix;
import org.cacheonix.cache.Cache;

import com.betsol.employeePortal.model.User;
import com.betsol.employeePortal.repository.UserRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import java.util.UUID;

@Service
public class AuthService {
    final static Logger logger = Logger.getLogger(AuthService.class);

    final Cacheonix cacheManager = Cacheonix.getInstance();
    final Cache<String, String> cache = cacheManager.getCache("auth_details");

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private User newuser;

    @Value("${microsoft.auth.accesscode.url}")
    private String Verificationurl;


    public ResponseEntity<HashMap<String, Object>> onRedirection(User user)  {

        HttpHeaders responseHeaders = new HttpHeaders();
        HashMap<String,Object> map = new HashMap<>();
        logger.info("User redirected after login");
        String hashedToken = DigestUtils.sha256Hex(user.getAccessToken());


        if(user==null){
            logger.info("Code not present, or not sent by Microsoft");
            return ResponseEntity.badRequest().body(null);
        }
        User verifiedUser = checkUser(user.getAccessToken());
        if(!verifiedUser.getId().equals(user.getId())){
            return ResponseEntity.badRequest().body(null);
        }

        String email = user.getMail();
        User user1 = userRepo.findByEmailId(email);
        if (user1 != null) {
            logger.info("Existing User logged in");
            cache.put(hashedToken, String.valueOf(user1.isAdmin()));
            map.put("userId", user1.getUserId());
            map.put("isAdmin", user1.isAdmin());
            map.put("userName", user1.getDisplayName());
            responseHeaders.add("Authorization", "Bearer " + hashedToken);
            return ResponseEntity.ok().headers(responseHeaders).body(map);
        }
        logger.info("New User Logged in, adding to DB");
        newuser.setUserId(UUID.randomUUID().toString());
        newuser.setDisplayName(user.getDisplayName());
        newuser.setMail(email);
        try{
            userRepo.save(newuser);
        }catch (Exception e){
            logger.info("user failed to get added to DB");
        }
        cache.put(hashedToken, String.valueOf(newuser.isAdmin()));
        map.put("userId", newuser.getUserId());
        map.put("isAdmin", newuser.isAdmin());
        map.put("userName", newuser.getDisplayName());
        responseHeaders.add("Authorization", "Bearer " + hashedToken);
        return ResponseEntity.ok().headers(responseHeaders).body(map);

    }

    private User checkUser(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        HttpEntity<User> userHttpEntity = restTemplate.exchange(Verificationurl,HttpMethod.GET,request,User.class);
        return userHttpEntity.getBody();
    }


    public Boolean validateToken(String authorization) {
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
        String token = authorization.substring("Bearer " .length());
        try{
            if(cache.containsKey(token)){
                cache.remove(token);
                printAll();
                return true;
            }
            else {
                return false;
            }
        }catch (Exception e){
            logger.info("Failed to remove user, after logout");
            return false;
        }

    }


    public void printAll() {
        System.out.println(cache.entrySet());
    }

    public Boolean makeAdmin(HashMap<String,String> map, String authorization) {
        String userid = map.get("userid");
        String token = authorization.substring("Bearer " .length());
        if(Boolean.parseBoolean(cache.get(token))){
            userRepo.editAdmin(userid);
            return true;
        }else{
            return false;
        }
    }

    public Iterable<User> getAllUsers(String authorization) {

        String token = authorization.substring("Bearer " .length());
        if(Boolean.parseBoolean(cache.get(token))){
            Iterable<User> users =  userRepo.findAll();
            return users;
        }else{
            return null;
        }

    }
}
