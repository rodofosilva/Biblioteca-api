package br.com.biblioteca.repository;

import br.com.biblioteca.model.Emprestimo;
import br.com.biblioteca.model.StatusEmprestimo;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    long countByNomeUsuarioIgnoreCaseAndStatusIn(String nome, Collection<StatusEmprestimo> status);
    boolean existsByNomeUsuarioIgnoreCaseAndLivroIdAndStatusIn(String nome, Long livroId, Collection<StatusEmprestimo> status);
    boolean existsByLivroIdAndStatusIn(Long livroId, Collection<StatusEmprestimo> status);
    List<Emprestimo> findByStatusAndDataPrevistaDevolucaoBefore(StatusEmprestimo status, LocalDate data);
}
