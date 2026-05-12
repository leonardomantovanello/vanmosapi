package com.vanmos.van.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "van")
public class Van {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Placa Mercosul (ABC1D23) ou padrão antigo (ABC-1234)
    @NotBlank(message = "Placa é obrigatória")
    @Pattern(regexp = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$|^[A-Z]{3}-?\\d{4}$",
             message = "Placa inválida. Use o formato Mercosul (ABC1D23) ou antigo (ABC-1234)")
    @Column(name = "placa", length = 10, unique = true, nullable = false)
    private String placa;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 50, message = "Modelo deve ter no máximo 50 caracteres")
    @Column(name = "modelo", length = 50, nullable = false)
    private String modelo;

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50, message = "Marca deve ter no máximo 50 caracteres")
    @Column(name = "marca", length = 50, nullable = false)
    private String marca;

    @Min(value = 1950, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    @Column(name = "ano")
    private Integer ano;

    @Min(value = 1,  message = "Capacidade mínima é 1")
    @Max(value = 50, message = "Capacidade máxima é 50")
    @Column(name = "capacidade")
    private Integer capacidade;

    @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
    @Column(name = "cor", length = 30)
    private String cor;

    // RENAVAM: 9 ou 11 dígitos
    @Pattern(regexp = "^\\d{9}(\\d{2})?$",
             message = "RENAVAM deve conter 9 ou 11 dígitos")
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