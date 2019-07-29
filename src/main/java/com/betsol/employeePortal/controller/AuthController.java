package com.betsol.employeePortal.controller;


import com.betsol.employeePortal.service.AuthService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;
    final static Logger logger = Logger.getLogger(AuthController.class);


    @GetMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response)
    {
        try{
            authService.loginService(request,response);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e){
            logger.error("login failed");
            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }

    }

    @GetMapping("/success" )
    public  ResponseEntity<?> onRedirect(HttpServletRequest request, HttpServletResponse response){
        try {
            logger.info("Redirected Successfully");
            return authService.onRedirection(request, response);
        }
        catch (Exception e){
            logger.error("redirection failed");
            return  new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader String authorization, @RequestHeader String userid) {
        try {
            Boolean result = authService.validateToken(authorization, userid);
            Map<String, Boolean> map = new HashMap<>();
            map.put("isAdmin", result);
            if (result == null) {
                logger.error("Authentication failed");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                logger.info("User authenticated successfully");
                return new ResponseEntity<Map>(map, HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error("Authentication failed");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestHeader String authorization)  {
        try {
            boolean result = authService.inValidate(authorization);
            if (result) {
                logger.info("user logged out");
                return new ResponseEntity<String>("logged out successfully", HttpStatus.OK);
            } else {
                logger.error("Log out failed");
                return new ResponseEntity<String>("", HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e){
            logger.error("logout failed");
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/print")
    public void printCache(){
        logger.info("printing cache");
        authService.printAll();
    }
}
