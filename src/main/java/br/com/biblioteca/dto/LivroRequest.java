package br.com.biblioteca.dto;

import jakarta.validation.constraints.*;

public record LivroRequest(
        @NotBlank(message = "Título é obrigatório") String titulo,
        @NotBlank(message = "Autor é obrigatório") String autor,
        @NotBlank(message = "Categoria é obrigatória") String categoria,
        @NotNull(message = "Quantidade total é obrigatória") @Min(value = 1, message = "Quantidade total deve ser no mínimo 1") Integer quantidadeTotal) {}
