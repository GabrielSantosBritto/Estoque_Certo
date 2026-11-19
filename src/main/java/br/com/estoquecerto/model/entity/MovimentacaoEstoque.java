package br.com.estoquecerto.model.entity;

import br.com.estoquecerto.model.enums.OrigemMovimentacao;
import br.com.estoquecerto.model.enums.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro histórico de uma entrada ou saída de estoque.
 *
 * Movimentações não são editadas nem apagadas. Uma correção é feita
 * com uma nova movimentação de origem AJUSTE.
 *
 * Novas movimentações devem ser criadas pelos geradores do pacote
 * factory (padrão Factory Method), e não diretamente com "new".
 */
public class MovimentacaoEstoque {

    private Long id;
    private Produto produto;
    private TipoMovimentacao tipo;
    private OrigemMovimentacao origem;
    private BigDecimal quantidade;
    private LocalDateTime dataMovimentacao;
    private Long itemCompraId;      // preenchido quando a origem é COMPRA
    private Long itemDevolucaoId;   // preenchido quando a origem é DEVOLUCAO
    private String motivo;          // obrigatório quando a origem é AJUSTE

    // Construtor completo: utilizado nas consultas ao banco
    public MovimentacaoEstoque(
            Long id,
            Produto produto,
            TipoMovimentacao tipo,
            OrigemMovimentacao origem,
            BigDecimal quantidade,
            LocalDateTime dataMovimentacao,
            Long itemCompraId,
            Long itemDevolucaoId,
            String motivo) {

        this.id = id;
        this.produto = produto;
        this.tipo = tipo;
        this.origem = origem;
        this.quantidade = quantidade;
        this.dataMovimentacao = dataMovimentacao;
        this.itemCompraId = itemCompraId;
        this.itemDevolucaoId = itemDevolucaoId;
        this.motivo = motivo;
    }

    // Construtor para nova movimentação: usado pelos geradores
    public MovimentacaoEstoque(
            Produto produto,
            TipoMovimentacao tipo,
            OrigemMovimentacao origem,
            BigDecimal quantidade,
            Long itemCompraId,
            Long itemDevolucaoId,
            String motivo) {

        this(null, produto, tipo, origem, quantidade,
             LocalDateTime.now(), itemCompraId, itemDevolucaoId, motivo);
    }

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public OrigemMovimentacao getOrigem() {
        return origem;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }

    public Long getItemCompraId() {
        return itemCompraId;
    }

    public Long getItemDevolucaoId() {
        return itemDevolucaoId;
    }

    public String getMotivo() {
        return motivo;
    }

    // Quanto esta movimentação soma (+) ou subtrai (-) do estoque
    public BigDecimal getVariacaoEstoque() {
        return tipo == TipoMovimentacao.ENTRADA
                ? quantidade
                : quantidade.negate();
    }
}
