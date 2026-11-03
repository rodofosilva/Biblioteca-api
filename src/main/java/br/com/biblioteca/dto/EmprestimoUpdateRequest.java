package br.com.biblioteca.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record EmprestimoUpdateRequest(
        @NotBlank(message = "Nome do usuário é obrigatório") String nomeUsuario,
        @NotNull(message = "Nova data prevista é obrigatória") LocalDate dataPrevistaDevolucao) {}
