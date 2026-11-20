package br.com.estoquecerto.repository;

import br.com.estoquecerto.infrastructure.database.DatabaseConnection;
import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.OrigemMovimentacao;
import br.com.estoquecerto.model.enums.TipoMovimentacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistência das movimentações de estoque.
 *
 * Movimentações são histórico: este repositório só insere e consulta.
 * Não existe update nem delete; correções são novas movimentações
 * de origem AJUSTE.
 */
public class MovimentacaoRepository {

    /**
     * Grava a movimentação usando a conexão da transação do Command.
     * Não abre nem fecha a conexão, e não faz commit: quem decide é
     * o ExecutorTransacional.
     */
    public Long salvar(Connection conexao, MovimentacaoEstoque movimentacao)
            throws SQLException {

        String sql = """
            INSERT INTO movimentacoes_estoque (
                produto_id,
                tipo,
                origem,
                quantidade,
                data_movimentacao,
                item_compra_id,
                item_devolucao_id,
                motivo
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement comando = conexao.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setLong(1, movimentacao.getProduto().getId());
            comando.setString(2, movimentacao.getTipo().name());
            comando.setString(3, movimentacao.getOrigem().name());
            comando.setBigDecimal(4, movimentacao.getQuantidade());
            comando.setTimestamp(5,
                Timestamp.valueOf(movimentacao.getDataMovimentacao()));
            definirLongOuNulo(comando, 6, movimentacao.getItemCompraId());
            definirLongOuNulo(comando, 7, movimentacao.getItemDevolucaoId());
            comando.setString(8, movimentacao.getMotivo());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getLong(1);
                }
            }
        }

        throw new SQLException(
            "Não foi possível obter o ID da movimentação registrada."
        );
    }

    // Histórico de um produto, da movimentação mais recente para a mais antiga
    public List<MovimentacaoEstoque> listarPorProduto(Long produtoId)
            throws SQLException {

        String sql = """
            SELECT
                m.id,
                m.tipo,
                m.origem,
                m.quantidade,
                m.data_movimentacao,
                m.item_compra_id,
                m.item_devolucao_id,
                m.motivo,
                p.id                  AS produto_id,
                p.codigo              AS produto_codigo,
                p.nome                AS produto_nome,
                p.descricao           AS produto_descricao,
                p.unidade_medida      AS produto_unidade_medida,
                p.quantidade_estoque  AS produto_quantidade_estoque,
                p.quantidade_minima   AS produto_quantidade_minima,
                p.preco_venda         AS produto_preco_venda,
                p.ativo               AS produto_ativo
            FROM movimentacoes_estoque m
            JOIN produtos p ON p.id = m.produto_id
            WHERE m.produto_id = ?
            ORDER BY m.data_movimentacao DESC, m.id DESC
            """;

        List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, produtoId);

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    movimentacoes.add(montarMovimentacao(resultado));
                }
            }
        }

        return movimentacoes;
    }

    private MovimentacaoEstoque montarMovimentacao(ResultSet resultado)
            throws SQLException {

        Produto produto = new Produto(
            resultado.getLong("produto_id"),
            resultado.getString("produto_codigo"),
            resultado.getString("produto_nome"),
            resultado.getString("produto_descricao"),
            resultado.getString("produto_unidade_medida"),
            resultado.getBigDecimal("produto_quantidade_estoque"),
            resultado.getBigDecimal("produto_quantidade_minima"),
            resultado.getBigDecimal("produto_preco_venda"),
            resultado.getBoolean("produto_ativo")
        );

        return new MovimentacaoEstoque(
            resultado.getLong("id"),
            produto,
            TipoMovimentacao.valueOf(resultado.getString("tipo")),
            OrigemMovimentacao.valueOf(resultado.getString("origem")),
            resultado.getBigDecimal("quantidade"),
            resultado.getTimestamp("data_movimentacao").toLocalDateTime(),
            resultado.getObject("item_compra_id", Long.class),
            resultado.getObject("item_devolucao_id", Long.class),
            resultado.getString("motivo")
        );
    }

    private void definirLongOuNulo(
            PreparedStatement comando,
            int posicao,
            Long valor) throws SQLException {

        if (valor == null) {
            comando.setNull(posicao, Types.BIGINT);
        } else {
            comando.setLong(posicao, valor);
        }
    }
}
