package com.betsol.employeePortal.controller;


import com.betsol.employeePortal.model.User;
import com.betsol.employeePortal.service.AuthService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;
    final static Logger logger = Logger.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User body)
    {
        System.out.println(body);
        try {
            logger.info("Redirected Successfully");
            return authService.onRedirection(body);
        }
        catch (Exception e){
            logger.error("redirection failed");
            return  new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader String authorization) {
        try {
            Boolean result = authService.validateToken(authorization);
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

    @PatchMapping("/editUser")
    public ResponseEntity makeAdmin(@RequestHeader String authorization,@RequestBody HashMap<String,String> body){
        try {
            Boolean result = authService.makeAdmin(body,authorization);

            if(result){
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getallusers(@RequestHeader String authorization){
        try {
            Iterable<User> users = authService.getAllUsers(authorization);

            if(users!=null){
                return new ResponseEntity<Iterable<User>>(users,HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
