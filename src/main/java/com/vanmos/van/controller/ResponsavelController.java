package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Responsavel;
import com.vanmos.van.model.service.ResponsavelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/responsaveis")
@CrossOrigin(origins = "*")
public class ResponsavelController {

    @Autowired
    private ResponsavelService responsavelService;

    @GetMapping
    public List<Responsavel> listarTodos() {
        return responsavelService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Responsavel> buscarPorId(@PathVariable Long id) {
        return responsavelService.findById(id);
    }

    @PostMapping
    public Responsavel criar(@RequestBody Responsavel responsavel) {
        return responsavelService.save(responsavel);
    }

    @PutMapping("/{id}")
    public Responsavel atualizar(@PathVariable Long id, @RequestBody Responsavel responsavel) {
        return responsavelService.update(id, responsavel);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        responsavelService.deleteById(id);
    }
}