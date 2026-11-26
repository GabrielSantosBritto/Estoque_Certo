package br.com.estoquecerto.command;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.factory.GeradorSaidaDevolucao;
import br.com.estoquecerto.model.entity.Devolucao;
import br.com.estoquecerto.model.entity.ItemDevolucao;
import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.enums.StatusDevolucao;
import br.com.estoquecerto.repository.DevolucaoRepository;
import br.com.estoquecerto.repository.MovimentacaoRepository;
import br.com.estoquecerto.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Comando concreto: conclui uma devolução ao fornecedor.
 *
 * Numa única transação:
 *   1. confere, para cada item, que o total devolvido (devoluções já
 *      concluídas + esta) não passa da quantidade comprada;
 *   2. muda a devolução de PENDENTE para CONCLUIDA;
 *   3. para cada item, cria uma movimentação de SAIDA com o
 *      GeradorSaidaDevolucao (Factory Method) e grava no histórico;
 *   4. subtrai a quantidade do estoque do produto.
 *
 * Se qualquer passo falhar, o ExecutorTransacional desfaz todos:
 * a devolução volta a PENDENTE e nenhum estoque é alterado.
 */
public class ConcluirDevolucaoCommand implements Command {

    private final Long devolucaoId;

    private final DevolucaoRepository devolucaoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    private int itensDevolvidos;

    public ConcluirDevolucaoCommand(Long devolucaoId) {
        this.devolucaoId = devolucaoId;
        this.devolucaoRepository = new DevolucaoRepository();
        this.movimentacaoRepository = new MovimentacaoRepository();
        this.produtoRepository = new ProdutoRepository();
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

        // Regra de estado na entidade: só PENDENTE pode ser concluída
        devolucao.concluir();

        List<Long> itemCompraIds = devolucao.getItens().stream()
                .map(item -> item.getItemCompra().getId())
                .toList();

        // Serializa conclusões simultâneas que envolvem os mesmos itens
        devolucaoRepository.bloquearItensCompra(conexao, itemCompraIds);

        for (ItemDevolucao item : devolucao.getItens()) {
            validarQuantidadeDevolvida(conexao, item);
        }

        // Troca atômica: impede que duas requisições concluam a mesma devolução
        if (!devolucaoRepository.atualizarStatus(
                conexao,
                devolucaoId,
                StatusDevolucao.PENDENTE,
                StatusDevolucao.CONCLUIDA)) {

            throw new ConflitoException(
                "A devolução foi alterada por outra operação. Tente novamente."
            );
        }

        for (ItemDevolucao item : devolucao.getItens()) {

            // Factory Method: o gerador decide tipo (SAIDA), origem
            // (DEVOLUCAO) e vínculo (item da devolução). Também recusa
            // saída maior que o estoque lido.
            MovimentacaoEstoque saida = new GeradorSaidaDevolucao(item.getId())
                    .registrar(item.getProduto(), item.getQuantidade());

            movimentacaoRepository.salvar(conexao, saida);

            // Proteção final: o UPDATE atômico não deixa o estoque negativo
            // mesmo que outra operação tenha mexido nele depois da leitura
            if (!produtoRepository.atualizarEstoque(
                    conexao,
                    item.getProduto().getId(),
                    saida.getVariacaoEstoque())) {

                throw new ConflitoException(
                    "Estoque insuficiente para o produto "
                    + item.getProduto().getCodigo() + "."
                );
            }

            itensDevolvidos++;
        }
    }

    private void validarQuantidadeDevolvida(
            Connection conexao,
            ItemDevolucao item) throws SQLException {

        BigDecimal jaDevolvido = devolucaoRepository
                .somarQuantidadeDevolvida(conexao, item.getItemCompra().getId());

        BigDecimal totalDevolvido = jaDevolvido.add(item.getQuantidade());

        if (totalDevolvido.compareTo(item.getItemCompra().getQuantidade()) > 0) {
            throw new ConflitoException(
                "A devolução do produto " + item.getProduto().getCodigo()
                + " ultrapassa a quantidade comprada ("
                + item.getItemCompra().getQuantidade().stripTrailingZeros()
                    .toPlainString()
                + "). Já devolvido: "
                + jaDevolvido.stripTrailingZeros().toPlainString() + "."
            );
        }
    }

    // Quantidade de itens que saíram do estoque; disponível após a execução
    public int getItensDevolvidos() {
        return itensDevolvidos;
    }
}
