package br.com.biblioteca.dto;

import java.time.LocalDateTime;

public record ErroResposta(int status, String erro, String mensagem, LocalDateTime timestamp) {}
