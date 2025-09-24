package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Responsavel;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/responsaveis")
public class ResponsavelController {

    private List<Responsavel> responsaveis = new ArrayList<>();

    @GetMapping
    public List<Responsavel> listarTodos() {
        return responsaveis;
    }

    @GetMapping("/findall")
    public List<Responsavel> findAll() {
        return responsaveis;
    }

    @GetMapping("/{id}")
    public Responsavel buscarPorId(@PathVariable Long id) {
        return responsaveis.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    @PostMapping
    public Responsavel criar(@RequestBody Responsavel responsavel) {
        responsavel.setId((long) (responsaveis.size() + 1));
        responsaveis.add(responsavel);
        return responsavel;
    }

    @PutMapping("/{id}")
    public Responsavel atualizar(@PathVariable Long id, @RequestBody Responsavel responsavel) {
        responsavel.setId(id);
        responsaveis.removeIf(r -> r.getId().equals(id));
        responsaveis.add(responsavel);
        return responsavel;
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        responsaveis.removeIf(r -> r.getId().equals(id));
    }
}