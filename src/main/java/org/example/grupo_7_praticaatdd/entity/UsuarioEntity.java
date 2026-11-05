package org.example.grupo_7_praticaatdd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.example.grupo_7_praticaatdd.domain.PlanoAssinatura;

// Camada ENTITY: guarda o estado do usuario no banco.
// As regras de negocio ficam no pacote domain, que e o codigo nascido do TDD.
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private int creditosCursos;

    @Column(nullable = false)
    private int cursosConcluidosComSucesso;

    // Colunas do B1 (Premium e moedas). Aceitam nulo para nao quebrar bancos ja populados na AC1.
    @Enumerated(EnumType.STRING)
    private PlanoAssinatura plano;

    private Integer moedas;

    protected UsuarioEntity() {
    }

    public UsuarioEntity(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getCreditosCursos() {
        return creditosCursos;
    }

    public int getCursosConcluidosComSucesso() {
        return cursosConcluidosComSucesso;
    }

    public void setCreditosCursos(int creditosCursos) {
        this.creditosCursos = creditosCursos;
    }

    public void setCursosConcluidosComSucesso(int cursosConcluidosComSucesso) {
        this.cursosConcluidosComSucesso = cursosConcluidosComSucesso;
    }

    public PlanoAssinatura getPlano() {
        return plano == null ? PlanoAssinatura.BASICO : plano;
    }

    public int getMoedas() {
        return moedas == null ? 0 : moedas;
    }

    public void setPlano(PlanoAssinatura plano) {
        this.plano = plano;
    }

    public void setMoedas(int moedas) {
        this.moedas = moedas;
    }
}
