package br.com.estoquecerto.factory;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.TipoMovimentacao;

import java.math.BigDecimal;

/**
 * Creator do padrão Factory Method (GoF).
 *
 * Declara o factory method criarMovimentacao(), e cada subclasse
 * decide qual movimentação criar (tipo, origem e vínculo):
 *
 *   GeradorEntradaCompra   -> ENTRADA / COMPRA     (recebe o itemCompraId)
 *   GeradorSaidaDevolucao  -> SAIDA   / DEVOLUCAO  (recebe o itemDevolucaoId)
 *   GeradorAjuste          -> ENTRADA ou SAIDA / AJUSTE (recebe tipo e motivo)
 *
 * Os dados específicos de cada origem são passados no construtor da
 * subclasse. Assim, o factory method tem a mesma assinatura para todas.
 *
 * Uma nova origem (por exemplo VENDA) é apenas uma nova subclasse,
 * sem alterar as existentes.
 *
 * Este gerador só CRIA a movimentação; quem grava no banco é o
 * MovimentacaoRepository, dentro da transação do Command.
 */
public abstract class GeradorMovimentacao {

    // Factory method: implementado por cada subclasse
    protected abstract MovimentacaoEstoque criarMovimentacao(
            Produto produto,
            BigDecimal quantidade);

    // Operação comum a todas as origens
    public final MovimentacaoEstoque registrar(
            Produto produto,
            BigDecimal quantidade) {

        if (produto == null) {
            throw new IllegalArgumentException(
                "Produto da movimentação não informado."
            );
        }

        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException(
                "A quantidade da movimentação deve ser maior que zero."
            );
        }

        MovimentacaoEstoque movimentacao =
            criarMovimentacao(produto, quantidade);

        if (movimentacao.getTipo() == TipoMovimentacao.SAIDA &&
            produto.getQuantidadeEstoque().compareTo(quantidade) < 0) {

            throw new ConflitoException(
                "Estoque insuficiente para o produto "
                + produto.getCodigo() + "."
            );
        }

        return movimentacao;
    }
}
