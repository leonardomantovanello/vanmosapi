package com.vanmos.van.model.entity;

public class Aluno {
    private Long id;
    private String nome;
    private String telefoneResponsavel;
    private String enderecoEmbarque;
    private String enderecoDesembarque;
    private String escola;
    private String turno;
    private String serie;
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

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
