package br.com.estoquecerto.model.entity;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.model.enums.MotivoDevolucao;
import br.com.estoquecerto.model.enums.StatusDevolucao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Devolução de mercadoria ao fornecedor, vinculada a uma compra recebida.
 *
 * As mudanças de status ficam na própria entidade, para que a regra
 * "só uma devolução PENDENTE pode ser concluída ou cancelada" valha em
 * qualquer ponto do sistema (mesmo desenho da Compra).
 *
 * A compra é carregada sem os itens: aqui ela serve para identificar
 * a compra, o fornecedor e a data. Os itens devolvidos apontam cada um
 * para o seu ItemCompra.
 */
public class Devolucao {

    private Long id;
    private Compra compra;
    private LocalDate dataDevolucao;
    private MotivoDevolucao motivo;
    private String descricao;
    private StatusDevolucao status;
    private final List<ItemDevolucao> itens = new ArrayList<>();

    // Construtor completo: utilizado nas consultas ao banco
    public Devolucao(
            Long id,
            Compra compra,
            LocalDate dataDevolucao,
            MotivoDevolucao motivo,
            String descricao,
            StatusDevolucao status) {

        this.id = id;
        this.compra = compra;
        this.dataDevolucao = dataDevolucao;
        this.motivo = motivo;
        this.descricao = descricao;
        this.status = status;
    }

    // Construtor para nova devolução: começa PENDENTE
    public Devolucao(
            Compra compra,
            LocalDate dataDevolucao,
            MotivoDevolucao motivo,
            String descricao) {

        this(
            null,
            compra,
            dataDevolucao != null ? dataDevolucao : LocalDate.now(),
            motivo,
            descricao,
            StatusDevolucao.PENDENTE
        );
    }

    // Itens

    public void adicionarItem(ItemDevolucao item) {

        if (status != StatusDevolucao.PENDENTE) {
            throw new ConflitoException(
                "Itens só podem ser alterados em devoluções pendentes."
            );
        }

        boolean itemRepetido = itens.stream()
                .anyMatch(existente -> existente.getItemCompra().getId()
                        .equals(item.getItemCompra().getId()));

        if (itemRepetido) {
            throw new IllegalArgumentException(
                "O item " + item.getItemCompra().getId()
                + " da compra aparece mais de uma vez na devolução."
            );
        }

        itens.add(item);
    }

    // Usado pelo repository ao montar a devolução lida do banco
    public void carregarItem(ItemDevolucao item) {
        itens.add(item);
    }

    public List<ItemDevolucao> getItens() {
        return Collections.unmodifiableList(itens);
    }

    // Mudanças de status

    public void concluir() {
        exigirPendente("concluir");
        this.status = StatusDevolucao.CONCLUIDA;
    }

    public void cancelar() {
        exigirPendente("cancelar");
        this.status = StatusDevolucao.CANCELADA;
    }

    private void exigirPendente(String acao) {

        if (status != StatusDevolucao.PENDENTE) {
            throw new ConflitoException(
                "Não é possível " + acao + " uma devolução com status "
                + status + ". Apenas devoluções PENDENTE."
            );
        }
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Compra getCompra() {
        return compra;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public MotivoDevolucao getMotivo() {
        return motivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusDevolucao getStatus() {
        return status;
    }
}
