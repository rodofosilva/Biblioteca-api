package br.com.biblioteca.service;

import br.com.biblioteca.dto.EmprestimoRequest;
import br.com.biblioteca.dto.EmprestimoUpdateRequest;
import br.com.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.biblioteca.exception.RegraNegocioException;
import br.com.biblioteca.model.Emprestimo;
import br.com.biblioteca.model.Livro;
import br.com.biblioteca.model.StatusEmprestimo;
import br.com.biblioteca.repository.EmprestimoRepository;
import br.com.biblioteca.repository.LivroRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmprestimoService {
    static final int LIMITE_ABERTOS_POR_USUARIO = 3;
    static final BigDecimal MULTA_POR_DIA = new BigDecimal("2.00");
    private static final List<StatusEmprestimo> EM_ABERTO = List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO);

    private final EmprestimoRepository repo;
    private final LivroRepository livros;

    /** Filtros opcionais: status, usuário, livro e período do empréstimo (de/ate). */
    @Transactional
    public List<Emprestimo> listar(StatusEmprestimo status, String usuario, Long livroId, LocalDate de, LocalDate ate) {
        atualizarAtrasados();
        return repo.findAll().stream()
                .filter(e -> status == null || e.getStatus() == status)
                .filter(e -> usuario == null || usuario.isBlank() || e.getNomeUsuario().toLowerCase().contains(usuario.trim().toLowerCase()))
                .filter(e -> livroId == null || e.getLivro().getId().equals(livroId))
                .filter(e -> de == null || !e.getDataEmprestimo().isBefore(de))
                .filter(e -> ate == null || !e.getDataEmprestimo().isAfter(ate))
                .toList();
    }

    @Transactional
    public Emprestimo buscar(Long id) {
        atualizarAtrasados();
        return repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo " + id + " não encontrado"));
    }

    /** Regras: estoque, limite de 3 em aberto por usuário e sem duplicar o mesmo livro para o mesmo usuário. */
    @Transactional
    public Emprestimo criar(EmprestimoRequest r) {
        Livro livro = livros.findById(r.livroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro " + r.livroId() + " não encontrado"));
        String nome = r.nomeUsuario().trim();
        if (livro.getQuantidadeDisponivel() <= 0)
            throw new RegraNegocioException("Livro sem estoque disponível");
        if (repo.existsByNomeUsuarioIgnoreCaseAndLivroIdAndStatusIn(nome, livro.getId(), EM_ABERTO))
            throw new RegraNegocioException("O usuário já possui um empréstimo em aberto deste livro");
        if (repo.countByNomeUsuarioIgnoreCaseAndStatusIn(nome, EM_ABERTO) >= LIMITE_ABERTOS_POR_USUARIO)
            throw new RegraNegocioException("Usuário atingiu o limite de " + LIMITE_ABERTOS_POR_USUARIO + " empréstimos em aberto");

        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() - 1);
        Emprestimo e = new Emprestimo();
        e.setLivro(livro);
        e.setNomeUsuario(nome);
        e.setDataEmprestimo(LocalDate.now());
        e.setDataPrevistaDevolucao(LocalDate.now().plusDays(r.dias()));
        e.setStatus(StatusEmprestimo.ATIVO);
        return repo.save(e);
    }

    /** Regra: só edita empréstimo em aberto, e a nova data não pode ser anterior ao empréstimo. */
    @Transactional
    public Emprestimo atualizar(Long id, EmprestimoUpdateRequest r) {
        Emprestimo e = buscar(id);
        if (e.getStatus() == StatusEmprestimo.DEVOLVIDO)
            throw new RegraNegocioException("Empréstimo já devolvido não pode ser alterado");
        if (r.dataPrevistaDevolucao().isBefore(e.getDataEmprestimo()))
            throw new RegraNegocioException("A data prevista não pode ser anterior à data do empréstimo");
        e.setNomeUsuario(r.nomeUsuario().trim());
        e.setDataPrevistaDevolucao(r.dataPrevistaDevolucao());
        e.setStatus(r.dataPrevistaDevolucao().isBefore(LocalDate.now()) ? StatusEmprestimo.ATRASADO : StatusEmprestimo.ATIVO);
        return repo.save(e);
    }

    /** Regra: devolução repõe o estoque e calcula multa de R$ 2,00 por dia de atraso. */
    @Transactional
    public Emprestimo devolver(Long id) {
        Emprestimo e = buscar(id);
        if (e.getStatus() == StatusEmprestimo.DEVOLVIDO)
            throw new RegraNegocioException("Este empréstimo já foi devolvido");
        LocalDate hoje = LocalDate.now();
        long diasAtraso = ChronoUnit.DAYS.between(e.getDataPrevistaDevolucao(), hoje);
        e.setMulta(diasAtraso > 0 ? MULTA_POR_DIA.multiply(BigDecimal.valueOf(diasAtraso)) : BigDecimal.ZERO);
        e.setDataDevolucao(hoje);
        e.setStatus(StatusEmprestimo.DEVOLVIDO);
        Livro livro = e.getLivro();
        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() + 1);
        return repo.save(e);
    }

    /** Regra: só é possível excluir empréstimos já devolvidos (histórico). */
    @Transactional
    public void deletar(Long id) {
        Emprestimo e = buscar(id);
        if (e.getStatus() != StatusEmprestimo.DEVOLVIDO)
            throw new RegraNegocioException("Só é possível excluir empréstimos já devolvidos");
        repo.delete(e);
    }

    private void atualizarAtrasados() {
        repo.findByStatusAndDataPrevistaDevolucaoBefore(StatusEmprestimo.ATIVO, LocalDate.now())
                .forEach(e -> e.setStatus(StatusEmprestimo.ATRASADO));
    }
}
