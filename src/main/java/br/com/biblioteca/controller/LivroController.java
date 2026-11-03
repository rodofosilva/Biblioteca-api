package br.com.biblioteca.controller;

import br.com.biblioteca.dto.LivroRequest;
import br.com.biblioteca.model.Livro;
import br.com.biblioteca.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/livros")
@RequiredArgsConstructor
@Tag(name = "Livros", description = "CRUD e filtros de livros")
public class LivroController {
    private final LivroService service;

    @GetMapping
    @Operation(summary = "Lista livros", description = "Filtros opcionais: categoria, autor e disponivel (true/false). Retorna 200.")
    public List<Livro> listar(@RequestParam(required = false) String categoria,
                              @RequestParam(required = false) String autor,
                              @RequestParam(required = false) Boolean disponivel) {
        return service.listar(categoria, autor, disponivel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca livro por id", description = "200 se existir, 404 se não encontrado.")
    public Livro buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra livro", description = "201 em caso de sucesso, 400 para dados inválidos.")
    public Livro criar(@Valid @RequestBody LivroRequest r) { return service.criar(r); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza livro", description = "200, 400, 404 ou 409 (total menor que exemplares emprestados).")
    public Livro atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequest r) { return service.atualizar(id, r); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui livro", description = "204, 404 ou 409 (há empréstimos em aberto ou histórico).")
    public void deletar(@PathVariable Long id) { service.deletar(id); }
}
