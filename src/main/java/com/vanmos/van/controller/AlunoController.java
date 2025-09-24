package com.vanmos.van.controller;

import com.vanmos.van.model.entity.Aluno;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private List<Aluno> alunos = new ArrayList<>();

    @GetMapping
    public List<Aluno> listarTodos() {
        return alunos;
    }

    @GetMapping("/findall")
    public List<Aluno> findAll() {
        return alunos;
    }

    @GetMapping("/{id}")
    public Aluno buscarPorId(@PathVariable Long id) {
        return alunos.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

    @PostMapping
    public Aluno criar(@RequestBody Aluno aluno) {
        aluno.setId((long) (alunos.size() + 1));
        alunos.add(aluno);
        return aluno;
    }

    @PutMapping("/{id}")
    public Aluno atualizar(@PathVariable Long id, @RequestBody Aluno aluno) {
        aluno.setId(id);
        alunos.removeIf(a -> a.getId().equals(id));
        alunos.add(aluno);
        return aluno;
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        alunos.removeIf(a -> a.getId().equals(id));
    }
}