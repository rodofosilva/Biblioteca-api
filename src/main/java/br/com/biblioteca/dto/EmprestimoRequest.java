package br.com.biblioteca.dto;

import jakarta.validation.constraints.*;

public record EmprestimoRequest(
        @NotNull(message = "livroId é obrigatório") Long livroId,
        @NotBlank(message = "Nome do usuário é obrigatório") String nomeUsuario,
        @NotNull(message = "Dias é obrigatório") @Min(value = 1, message = "Mínimo 1 dia") @Max(value = 30, message = "Máximo 30 dias") Integer dias) {}
