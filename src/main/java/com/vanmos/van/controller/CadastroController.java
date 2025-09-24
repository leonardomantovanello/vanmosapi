package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Cadastro;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cadastro")
public class CadastroController {

    private List<Cadastro> cadastros = new ArrayList<>();

    @PostMapping
    public Cadastro cadastrar(@RequestBody Cadastro cadastro) {
        cadastro.setId((long) (cadastros.size() + 1));
        cadastros.add(cadastro);
        return cadastro;
    }
}