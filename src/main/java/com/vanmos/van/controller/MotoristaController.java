package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Motorista;
import com.vanmos.van.model.service.MotoristaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/motoristas")
@CrossOrigin(origins = "*")
public class MotoristaController {

    @Autowired
    private MotoristaService motoristaService;

    @GetMapping
    public List<Motorista> listarTodos() {
        return motoristaService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Motorista> buscarPorId(@PathVariable Long id) {
        return motoristaService.findById(id);
    }

    @PostMapping
    public Motorista criar(@RequestBody Motorista motorista) {
        return motoristaService.save(motorista);
    }

    @PutMapping("/{id}")
    public Motorista atualizar(@PathVariable Long id, @RequestBody Motorista motorista) {
        return motoristaService.update(id, motorista);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        motoristaService.deleteById(id);
    }
}