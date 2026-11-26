package br.com.estoquecerto.command;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.model.entity.Devolucao;
import br.com.estoquecerto.model.enums.StatusDevolucao;
import br.com.estoquecerto.repository.DevolucaoRepository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Comando concreto: cancela uma devolução pendente.
 *
 * Cancelar não movimenta estoque, porque nada saiu ainda.
 */
public class CancelarDevolucaoCommand implements Command {

    private final Long devolucaoId;
    private final DevolucaoRepository devolucaoRepository;

    public CancelarDevolucaoCommand(Long devolucaoId) {
        this.devolucaoId = devolucaoId;
        this.devolucaoRepository = new DevolucaoRepository();
    }

    @Override
    public void executar(Connection conexao) throws SQLException {

        Devolucao devolucao = devolucaoRepository
                .buscarPorId(conexao, devolucaoId)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException(
                        "Devolução não encontrada."
                    )
                );

        // Regra de estado na entidade: só PENDENTE pode ser cancelada
        devolucao.cancelar();

        // Troca atômica: falha se outra requisição mudou o status antes
        if (!devolucaoRepository.atualizarStatus(
                conexao,
                devolucaoId,
                StatusDevolucao.PENDENTE,
                StatusDevolucao.CANCELADA)) {

            throw new ConflitoException(
                "A devolução foi alterada por outra operação. Tente novamente."
            );
        }
    }
}
