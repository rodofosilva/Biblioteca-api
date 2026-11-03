package br.com.biblioteca.controller;

import br.com.biblioteca.dto.EmprestimoRequest;
import br.com.biblioteca.dto.EmprestimoUpdateRequest;
import br.com.biblioteca.model.Emprestimo;
import br.com.biblioteca.model.StatusEmprestimo;
import br.com.biblioteca.service.EmprestimoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emprestimos")
@RequiredArgsConstructor
@Tag(name = "Empréstimos", description = "CRUD, filtros e regras de negócio de empréstimos")
public class EmprestimoController {
    private final EmprestimoService service;

    @GetMapping
    @Operation(summary = "Lista empréstimos", description = "Filtros opcionais: status, usuario, livroId, de e ate (yyyy-MM-dd, data do empréstimo).")
    public List<Emprestimo> listar(@RequestParam(required = false) StatusEmprestimo status,
                                   @RequestParam(required = false) String usuario,
                                   @RequestParam(required = false) Long livroId,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return service.listar(status, usuario, livroId, de, ate);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca empréstimo por id", description = "200 ou 404.")
    public Emprestimo buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Realiza empréstimo",
            description = "201. Regras: livro com estoque, máximo de 3 empréstimos em aberto por usuário e sem duplicar o mesmo livro (409). 404 se o livro não existir.")
    public Emprestimo criar(@Valid @RequestBody EmprestimoRequest r) { return service.criar(r); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza empréstimo em aberto", description = "Altera usuário e data prevista. 409 se já devolvido.")
    public Emprestimo atualizar(@PathVariable Long id, @Valid @RequestBody EmprestimoUpdateRequest r) { return service.atualizar(id, r); }

    @PatchMapping("/{id}/devolucao")
    @Operation(summary = "Registra devolução", description = "Repõe o exemplar no estoque e calcula multa de R$ 2,00/dia de atraso. 409 se já devolvido.")
    public Emprestimo devolver(@PathVariable Long id) { return service.devolver(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui empréstimo", description = "204. 409 se ainda não foi devolvido.")
    public void deletar(@PathVariable Long id) { service.deletar(id); }
}
