package br.com.estoquecerto.factory;

import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.OrigemMovimentacao;
import br.com.estoquecerto.model.enums.TipoMovimentacao;

import java.math.BigDecimal;

/**
 * Concrete Creator: ajuste manual de estoque (inventário, perda, avaria,
 * correção de lançamento).
 *
 * Diferente das outras origens, o ajuste pode ser ENTRADA ou SAIDA e
 * exige um motivo, porque não há documento (compra ou devolução)
 * que justifique a movimentação.
 */
public class GeradorAjuste extends GeradorMovimentacao {

    private static final int TAMANHO_MAXIMO_MOTIVO = 255;

    private final TipoMovimentacao tipo;
    private final String motivo;

    public GeradorAjuste(TipoMovimentacao tipo, String motivo) {

        if (tipo == null) {
            throw new IllegalArgumentException(
                "Tipo do ajuste não informado (ENTRADA ou SAIDA)."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                "O motivo é obrigatório para ajustes de estoque."
            );
        }

        if (motivo.strip().length() > TAMANHO_MAXIMO_MOTIVO) {
            throw new IllegalArgumentException(
                "O motivo deve ter no máximo "
                + TAMANHO_MAXIMO_MOTIVO + " caracteres."
            );
        }

        this.tipo = tipo;
        this.motivo = motivo.strip();
    }

    @Override
    protected MovimentacaoEstoque criarMovimentacao(
            Produto produto,
            BigDecimal quantidade) {

        return new MovimentacaoEstoque(
            produto,
            tipo,
            OrigemMovimentacao.AJUSTE,
            quantidade,
            null,
            null,
            motivo
        );
    }
}
