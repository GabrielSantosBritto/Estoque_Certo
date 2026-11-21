package br.com.estoquecerto.command;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.factory.GeradorEntradaCompra;
import br.com.estoquecerto.model.entity.Compra;
import br.com.estoquecerto.model.entity.ItemCompra;
import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.enums.StatusCompra;
import br.com.estoquecerto.repository.CompraRepository;
import br.com.estoquecerto.repository.MovimentacaoRepository;
import br.com.estoquecerto.repository.ProdutoRepository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Comando concreto: confirma o recebimento de uma compra.
 *
 * Numa única transação:
 *   1. muda a compra de PENDENTE para RECEBIDA;
 *   2. para cada item, cria uma movimentação de ENTRADA com o
 *      GeradorEntradaCompra (Factory Method) e grava no histórico;
 *   3. soma a quantidade ao estoque do produto.
 *
 * Se qualquer passo falhar, o ExecutorTransacional desfaz todos:
 * a compra volta a PENDENTE e nenhum estoque é alterado.
 */
public class ConfirmarRecebimentoCommand implements Command {

    private final Long compraId;

    private final CompraRepository compraRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    private int itensRecebidos;

    public ConfirmarRecebimentoCommand(Long compraId) {
        this.compraId = compraId;
        this.compraRepository = new CompraRepository();
        this.movimentacaoRepository = new MovimentacaoRepository();
        this.produtoRepository = new ProdutoRepository();
    }

    @Override
    public void executar(Connection conexao) throws SQLException {

        Compra compra = compraRepository.buscarPorId(conexao, compraId)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("Compra não encontrada.")
                );

        // Regra de estado na entidade: só PENDENTE pode ser recebida
        compra.marcarComoRecebida();

        // Troca atômica: impede que duas requisições recebam a mesma compra
        if (!compraRepository.atualizarStatus(
                conexao,
                compraId,
                StatusCompra.PENDENTE,
                StatusCompra.RECEBIDA)) {

            throw new ConflitoException(
                "A compra foi alterada por outra operação. Tente novamente."
            );
        }

        for (ItemCompra item : compra.getItens()) {

            // Factory Method: o gerador decide tipo (ENTRADA), origem
            // (COMPRA) e vínculo (item da compra) da movimentação
            MovimentacaoEstoque entrada = new GeradorEntradaCompra(item.getId())
                    .registrar(item.getProduto(), item.getQuantidade());

            movimentacaoRepository.salvar(conexao, entrada);

            if (!produtoRepository.atualizarEstoque(
                    conexao,
                    item.getProduto().getId(),
                    entrada.getVariacaoEstoque())) {

                throw new ConflitoException(
                    "Não foi possível atualizar o estoque do produto "
                    + item.getProduto().getCodigo() + "."
                );
            }

            itensRecebidos++;
        }
    }

    // Quantidade de itens que deram entrada; disponível após a execução
    public int getItensRecebidos() {
        return itensRecebidos;
    }
}
