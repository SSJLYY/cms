package com.resource.platform.controller;

import com.resource.platform.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/encode-password")
    public Result<Map<String, String>> encodePassword(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        Map<String, String> result = new HashMap<>();
        result.put("rawPassword", password);
        result.put("encodedPassword", encoded);
        return Result.success(result);
    }

    @GetMapping("/verify-password")
    public Result<Map<String, Object>> verifyPassword(
            @RequestParam String rawPassword,
            @RequestParam String encodedPassword) {
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        Map<String, Object> result = new HashMap<>();
        result.put("rawPassword", rawPassword);
        result.put("encodedPassword", encodedPassword);
        result.put("matches", matches);
        return Result.success(result);
    }
}
