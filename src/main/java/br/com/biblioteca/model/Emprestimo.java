package br.com.biblioteca.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Emprestimo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String nomeUsuario;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucao;
    @Enumerated(EnumType.STRING) private StatusEmprestimo status;
    private BigDecimal multa = BigDecimal.ZERO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id")
    private Livro livro;
}
