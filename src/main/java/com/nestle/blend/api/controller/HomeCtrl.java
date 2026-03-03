package com.nestle.blend.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeCtrl extends ABaseCtrl {

    @Value("${app.version}")
    private String appVersion;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public HomeCtrl(HttpServletRequest req, HttpServletResponse res) {
        super(req, res);
    }

    @GetMapping("/get-version")
    public String getAppVersion() {
        return "App version : " + this.appVersion;
    }

    @GetMapping("/gen-password")
    public String genPassword(@RequestParam String password) {
        return this.passwordEncoder.encode(password);
    }

}
