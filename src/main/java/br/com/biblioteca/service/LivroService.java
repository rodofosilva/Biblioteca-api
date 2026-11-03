package br.com.biblioteca.service;

import br.com.biblioteca.dto.LivroRequest;
import br.com.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.biblioteca.exception.RegraNegocioException;
import br.com.biblioteca.model.Livro;
import br.com.biblioteca.model.StatusEmprestimo;
import br.com.biblioteca.repository.EmprestimoRepository;
import br.com.biblioteca.repository.LivroRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LivroService {
    private final LivroRepository repo;
    private final EmprestimoRepository emprestimos;

    /** Filtros opcionais: categoria (exata), autor (contém) e disponibilidade. */
    public List<Livro> listar(String categoria, String autor, Boolean disponivel) {
        return repo.findAll().stream()
                .filter(l -> vazio(categoria) || l.getCategoria().equalsIgnoreCase(categoria.trim()))
                .filter(l -> vazio(autor) || l.getAutor().toLowerCase().contains(autor.trim().toLowerCase()))
                .filter(l -> disponivel == null || (l.getQuantidadeDisponivel() > 0) == disponivel)
                .toList();
    }

    public Livro buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Livro " + id + " não encontrado"));
    }

    @Transactional
    public Livro criar(LivroRequest r) {
        Livro l = new Livro();
        l.setQuantidadeDisponivel(r.quantidadeTotal());
        return repo.save(preencher(l, r));
    }

    /** Regra: ajustar o total mantém consistente a quantidade disponível (não pode ficar negativa). */
    @Transactional
    public Livro atualizar(Long id, LivroRequest r) {
        Livro l = buscar(id);
        int novoDisponivel = l.getQuantidadeDisponivel() + (r.quantidadeTotal() - l.getQuantidadeTotal());
        if (novoDisponivel < 0)
            throw new RegraNegocioException("Quantidade total menor que o número de exemplares atualmente emprestados");
        l.setQuantidadeDisponivel(novoDisponivel);
        return repo.save(preencher(l, r));
    }

    /** Regra: não pode excluir livro com empréstimo em aberto ou com histórico. */
    @Transactional
    public void deletar(Long id) {
        Livro l = buscar(id);
        if (emprestimos.existsByLivroIdAndStatusIn(id, List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO)))
            throw new RegraNegocioException("Não é possível excluir um livro com empréstimos em aberto");
        if (!l.getEmprestimos().isEmpty())
            throw new RegraNegocioException("Livro possui histórico de empréstimos; exclua os empréstimos devolvidos antes");
        repo.delete(l);
    }

    private Livro preencher(Livro l, LivroRequest r) {
        l.setTitulo(r.titulo().trim());
        l.setAutor(r.autor().trim());
        l.setCategoria(r.categoria().trim());
        l.setQuantidadeTotal(r.quantidadeTotal());
        return l;
    }

    private boolean vazio(String s) { return s == null || s.isBlank(); }
}
