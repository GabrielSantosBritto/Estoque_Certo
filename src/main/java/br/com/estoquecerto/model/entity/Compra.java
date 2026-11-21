package br.com.estoquecerto.model.entity;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.model.enums.StatusCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aquisição feita junto a um fornecedor.
 *
 * O total não é armazenado: é sempre calculado a partir dos itens,
 * do frete e do desconto.
 *
 * As mudanças de status ficam aqui, na própria entidade, para que a
 * regra "só uma compra PENDENTE pode ser recebida ou cancelada" valha
 * em qualquer ponto do sistema.
 */
public class Compra {

    private Long id;
    private Fornecedor fornecedor;
    private LocalDate dataCompra;
    private BigDecimal frete;
    private BigDecimal desconto;
    private StatusCompra status;
    private final List<ItemCompra> itens = new ArrayList<>();

    // Construtor completo: utilizado nas consultas ao banco
    public Compra(
            Long id,
            Fornecedor fornecedor,
            LocalDate dataCompra,
            BigDecimal frete,
            BigDecimal desconto,
            StatusCompra status) {

        this.id = id;
        this.fornecedor = fornecedor;
        this.dataCompra = dataCompra;
        this.frete = frete;
        this.desconto = desconto;
        this.status = status;
    }

    // Construtor para nova compra: começa PENDENTE
    public Compra(
            Fornecedor fornecedor,
            LocalDate dataCompra,
            BigDecimal frete,
            BigDecimal desconto) {

        this(
            null,
            fornecedor,
            dataCompra != null ? dataCompra : LocalDate.now(),
            frete != null ? frete : BigDecimal.ZERO,
            desconto != null ? desconto : BigDecimal.ZERO,
            StatusCompra.PENDENTE
        );
    }

    // Itens

    public void adicionarItem(ItemCompra item) {

        if (status != StatusCompra.PENDENTE) {
            throw new ConflitoException(
                "Itens só podem ser alterados em compras pendentes."
            );
        }

        boolean produtoRepetido = itens.stream()
                .anyMatch(existente -> existente.getProduto().getId()
                        .equals(item.getProduto().getId()));

        if (produtoRepetido) {
            throw new IllegalArgumentException(
                "O produto " + item.getProduto().getCodigo()
                + " aparece mais de uma vez na compra."
            );
        }

        itens.add(item);
    }

    // Usado pelo repository ao montar a compra lida do banco
    public void carregarItem(ItemCompra item) {
        itens.add(item);
    }

    public List<ItemCompra> getItens() {
        return Collections.unmodifiableList(itens);
    }

    // Totais (calculados, nunca armazenados)

    public BigDecimal getSubtotalItens() {
        return itens.stream()
                .map(ItemCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getSubtotalItens().add(frete).subtract(desconto);
    }

    // Mudanças de status

    public void marcarComoRecebida() {
        exigirPendente("receber");
        this.status = StatusCompra.RECEBIDA;
    }

    public void cancelar() {
        exigirPendente("cancelar");
        this.status = StatusCompra.CANCELADA;
    }

    private void exigirPendente(String acao) {

        if (status != StatusCompra.PENDENTE) {
            throw new ConflitoException(
                "Não é possível " + acao + " uma compra com status "
                + status + ". Apenas compras PENDENTE."
            );
        }
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public StatusCompra getStatus() {
        return status;
    }
}
