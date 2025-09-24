package com.vanmos.van.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "van")
public class Van {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "placa", length = 10, unique = true, nullable = false)
    private String placa;
    
    @Column(name = "modelo", length = 50)
    private String modelo;
    
    @Column(name = "marca", length = 50)
    private String marca;
    
    @Column(name = "ano")
    private Integer ano;
    
    @Column(name = "capacidade")
    private Integer capacidade;
    
    @Column(name = "cor", length = 30)
    private String cor;
    
    @Column(name = "renavam", length = 15, unique = true)
    private String renavam;
    
    @Column(name = "ativa")
    private boolean ativa;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getRenavam() { return renavam; }
    public void setRenavam(String renavam) { this.renavam = renavam; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}