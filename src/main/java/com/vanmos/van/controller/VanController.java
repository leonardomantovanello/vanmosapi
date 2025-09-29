package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Van;
import com.vanmos.van.model.service.VanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vans")
@CrossOrigin(origins = "*")
public class VanController {

    @Autowired
    private VanService vanService;

    @GetMapping
    public List<Van> listarTodas() {
        return vanService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Van> buscarPorId(@PathVariable Long id) {
        return vanService.findById(id);
    }

    @PostMapping
    public Van criar(@RequestBody Van van) {
        return vanService.save(van);
    }

    @PutMapping("/{id}")
    public Van atualizar(@PathVariable Long id, @RequestBody Van van) {
        return vanService.update(id, van);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        vanService.deleteById(id);
    }
}