package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Motorista;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/motoristas")
public class MotoristaController {

    private List<Motorista> motoristas = new ArrayList<>();

    @GetMapping
    public List<Motorista> listarTodos() {
        return motoristas;
    }

    @GetMapping("/findall")
    public List<Motorista> findAll() {
        return motoristas;
    }

    @GetMapping("/{id}")
    public Motorista buscarPorId(@PathVariable Long id) {
        return motoristas.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }

    @PostMapping
    public Motorista criar(@RequestBody Motorista motorista) {
        motorista.setId((long) (motoristas.size() + 1));
        motoristas.add(motorista);
        return motorista;
    }

    @PutMapping("/{id}")
    public Motorista atualizar(@PathVariable Long id, @RequestBody Motorista motorista) {
        motorista.setId(id);
        motoristas.removeIf(m -> m.getId().equals(id));
        motoristas.add(motorista);
        return motorista;
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        motoristas.removeIf(m -> m.getId().equals(id));
    }
}