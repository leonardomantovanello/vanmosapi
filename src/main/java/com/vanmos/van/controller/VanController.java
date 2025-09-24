package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Van;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/vans")
public class VanController {

    private List<Van> vans = new ArrayList<>();

    @GetMapping
    public List<Van> listarTodas() {
        return vans;
    }

    @GetMapping("/findall")
    public List<Van> findAll() {
        return vans;
    }

    @GetMapping("/{id}")
    public Van buscarPorId(@PathVariable Long id) {
        return vans.stream().filter(v -> v.getId().equals(id)).findFirst().orElse(null);
    }

    @PostMapping
    public Van criar(@RequestBody Van van) {
        van.setId((long) (vans.size() + 1));
        vans.add(van);
        return van;
    }

    @PutMapping("/{id}")
    public Van atualizar(@PathVariable Long id, @RequestBody Van van) {
        van.setId(id);
        vans.removeIf(v -> v.getId().equals(id));
        vans.add(van);
        return van;
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        vans.removeIf(v -> v.getId().equals(id));
    }
}