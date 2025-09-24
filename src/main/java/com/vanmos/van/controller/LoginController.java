package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Login;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private List<Login> logins = new ArrayList<>();

    @PostMapping
    public String login(@RequestBody Login login) {
        logins.add(login);
        return "Login realizado com sucesso";
    }

    @PostMapping("/logout")
    public String logout() {
        return "Logout realizado com sucesso";
    }
}