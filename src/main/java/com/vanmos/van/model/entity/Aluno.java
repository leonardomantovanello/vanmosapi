package com.vanmos.van.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "alunos")
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;
    
    @Column(name = "telefone_responsavel", length = 15)
    private String telefoneResponsavel;
    
    @Column(name = "endereco_embarque", length = 200)
    private String enderecoEmbarque;
    
    @Column(name = "endereco_desembarque", length = 200)
    private String enderecoDesembarque;
    
    @Column(name = "escola", length = 100)
    private String escola;
    
    @Column(name = "turno", length = 20)
    private String turno;

    
    @Column(name = "ativo")
    private boolean ativo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefoneResponsavel() { return telefoneResponsavel; }
    public void setTelefoneResponsavel(String telefoneResponsavel) { this.telefoneResponsavel = telefoneResponsavel; }

    public String getEnderecoEmbarque() { return enderecoEmbarque; }
    public void setEnderecoEmbarque(String enderecoEmbarque) { this.enderecoEmbarque = enderecoEmbarque; }

    public String getEnderecoDesembarque() { return enderecoDesembarque; }
    public void setEnderecoDesembarque(String enderecoDesembarque) { this.enderecoDesembarque = enderecoDesembarque; }

    public String getEscola() { return escola; }
    public void setEscola(String escola) { this.escola = escola; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }


    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
