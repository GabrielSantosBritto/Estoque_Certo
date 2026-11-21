package br.com.estoquecerto.command;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.model.entity.Compra;
import br.com.estoquecerto.model.enums.StatusCompra;
import br.com.estoquecerto.repository.CompraRepository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Comando concreto: cancela uma compra pendente.
 *
 * Cancelar não movimenta estoque, porque a mercadoria ainda não entrou.
 */
public class CancelarCompraCommand implements Command {

    private final Long compraId;
    private final CompraRepository compraRepository;

    public CancelarCompraCommand(Long compraId) {
        this.compraId = compraId;
        this.compraRepository = new CompraRepository();
    }

    @Override
    public void executar(Connection conexao) throws SQLException {

        Compra compra = compraRepository.buscarPorId(conexao, compraId)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("Compra não encontrada.")
                );

        // Regra de estado na entidade: só PENDENTE pode ser cancelada
        compra.cancelar();

        // Troca atômica: falha se outra requisição mudou o status antes
        if (!compraRepository.atualizarStatus(
                conexao,
                compraId,
                StatusCompra.PENDENTE,
                StatusCompra.CANCELADA)) {

            throw new ConflitoException(
                "A compra foi alterada por outra operação. Tente novamente."
            );
        }
    }
}
