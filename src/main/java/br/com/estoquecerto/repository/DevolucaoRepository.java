package br.com.estoquecerto.repository;

import br.com.estoquecerto.infrastructure.database.DatabaseConnection;
import br.com.estoquecerto.model.entity.Compra;
import br.com.estoquecerto.model.entity.Devolucao;
import br.com.estoquecerto.model.entity.Fornecedor;
import br.com.estoquecerto.model.entity.ItemCompra;
import br.com.estoquecerto.model.entity.ItemDevolucao;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.MotivoDevolucao;
import br.com.estoquecerto.model.enums.StatusCompra;
import br.com.estoquecerto.model.enums.StatusDevolucao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistência das devoluções e de seus itens.
 *
 * Métodos que recebem Connection participam da transação de um Command
 * (não abrem, não fecham e não fazem commit). Os demais são consultas
 * avulsas e abrem a própria conexão.
 */
public class DevolucaoRepository {

    private static final String SELECT_DEVOLUCAO = """
        SELECT
            d.id                  AS d_id,
            d.data_devolucao      AS d_data_devolucao,
            d.motivo              AS d_motivo,
            d.descricao           AS d_descricao,
            d.status              AS d_status,

            c.id                  AS c_id,
            c.data_compra         AS c_data_compra,
            c.frete               AS c_frete,
            c.desconto            AS c_desconto,
            c.status              AS c_status,

            f.id                  AS f_id,
            f.cnpj                AS f_cnpj,
            f.razao_social        AS f_razao_social,
            f.nome_fantasia       AS f_nome_fantasia,
            f.inscricao_estadual  AS f_inscricao_estadual,
            f.telefone            AS f_telefone,
            f.email               AS f_email,
            f.cep                 AS f_cep,
            f.logradouro          AS f_logradouro,
            f.numero              AS f_numero,
            f.complemento         AS f_complemento,
            f.bairro              AS f_bairro,
            f.municipio           AS f_municipio,
            f.uf                  AS f_uf,
            f.observacoes         AS f_observacoes,
            f.ativo               AS f_ativo

        FROM devolucoes d
        INNER JOIN compras c      ON c.id = d.compra_id
        INNER JOIN fornecedores f ON f.id = c.fornecedor_id
        """;

    private static final String SELECT_ITENS = """
        SELECT
            i.id                  AS i_id,
            i.devolucao_id        AS i_devolucao_id,
            i.quantidade          AS i_quantidade,

            ic.id                 AS ic_id,
            ic.compra_id          AS ic_compra_id,
            ic.quantidade         AS ic_quantidade,
            ic.preco_unitario     AS ic_preco_unitario,

            p.id                  AS p_id,
            p.codigo              AS p_codigo,
            p.nome                AS p_nome,
            p.descricao           AS p_descricao,
            p.unidade_medida      AS p_unidade_medida,
            p.quantidade_estoque  AS p_quantidade_estoque,
            p.quantidade_minima   AS p_quantidade_minima,
            p.preco_venda         AS p_preco_venda,
            p.ativo               AS p_ativo

        FROM itens_devolucao i
        INNER JOIN itens_compra ic ON ic.id = i.item_compra_id
        INNER JOIN produtos p      ON p.id = ic.produto_id
        """;

    // =========================
    // GRAVAÇÃO (dentro de transação)
    // =========================

    // Grava a devolução e todos os itens. Retorna o ID da devolução.
    public Long salvar(Connection conexao, Devolucao devolucao)
            throws SQLException {

        String sqlDevolucao = """
            INSERT INTO devolucoes (
                compra_id,
                data_devolucao,
                motivo,
                descricao,
                status
            ) VALUES (?, ?, ?, ?, ?)
            """;

        Long devolucaoId;

        try (PreparedStatement comando = conexao.prepareStatement(
                sqlDevolucao, Statement.RETURN_GENERATED_KEYS)) {

            comando.setLong(1, devolucao.getCompra().getId());
            comando.setDate(2, Date.valueOf(devolucao.getDataDevolucao()));
            comando.setString(3, devolucao.getMotivo().name());
            comando.setString(4, devolucao.getDescricao());
            comando.setString(5, devolucao.getStatus().name());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (!resultado.next()) {
                    throw new SQLException(
                        "Não foi possível obter o ID da devolução cadastrada."
                    );
                }
                devolucaoId = resultado.getLong(1);
            }
        }

        String sqlItem = """
            INSERT INTO itens_devolucao (
                devolucao_id,
                item_compra_id,
                quantidade
            ) VALUES (?, ?, ?)
            """;

        try (PreparedStatement comando = conexao.prepareStatement(sqlItem)) {

            for (ItemDevolucao item : devolucao.getItens()) {
                comando.setLong(1, devolucaoId);
                comando.setLong(2, item.getItemCompra().getId());
                comando.setBigDecimal(3, item.getQuantidade());
                comando.addBatch();
            }

            comando.executeBatch();
        }

        return devolucaoId;
    }

    /**
     * Troca o status somente se a devolução ainda estiver no status
     * esperado. A condição no WHERE torna a troca atômica: de duas
     * requisições simultâneas, só uma altera a linha.
     */
    public boolean atualizarStatus(
            Connection conexao,
            Long devolucaoId,
            StatusDevolucao statusEsperado,
            StatusDevolucao novoStatus) throws SQLException {

        String sql = """
            UPDATE devolucoes
            SET status = ?
            WHERE id = ? AND status = ?
            """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, novoStatus.name());
            comando.setLong(2, devolucaoId);
            comando.setString(3, statusEsperado.name());

            return comando.executeUpdate() > 0;
        }
    }

    /**
     * Bloqueia as linhas dos itens de compra até o fim da transação.
     *
     * Duas devoluções diferentes do mesmo item, concluídas ao mesmo tempo,
     * passam a ser processadas uma depois da outra. Assim a soma do que já
     * foi devolvido é sempre conferida com o valor mais recente.
     */
    public void bloquearItensCompra(
            Connection conexao,
            Collection<Long> itemCompraIds) throws SQLException {

        if (itemCompraIds.isEmpty()) {
            return;
        }

        String marcadores = String.join(
            ", ", Collections.nCopies(itemCompraIds.size(), "?")
        );

        String sql = "SELECT id FROM itens_compra WHERE id IN ("
            + marcadores + ") ORDER BY id FOR UPDATE";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            int posicao = 1;
            for (Long id : itemCompraIds) {
                comando.setLong(posicao++, id);
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    // só percorre: o objetivo é o bloqueio das linhas
                }
            }
        }
    }

    /**
     * Quanto de um item de compra já foi devolvido em devoluções
     * CONCLUIDAS. Pendentes e canceladas não contam, porque não tiraram
     * nada do estoque.
     *
     * FOR SHARE faz a consulta ler o dado mais recente já confirmado,
     * e não a "foto" do início da transação.
     */
    public BigDecimal somarQuantidadeDevolvida(
            Connection conexao,
            Long itemCompraId) throws SQLException {

        String sql = """
            SELECT COALESCE(SUM(i.quantidade), 0)
            FROM itens_devolucao i
            INNER JOIN devolucoes d ON d.id = i.devolucao_id
            WHERE i.item_compra_id = ?
              AND d.status = 'CONCLUIDA'
            FOR SHARE
            """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, itemCompraId);

            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBigDecimal(1);
            }
        }
    }

    // Versão avulsa, usada pelo Service na validação do cadastro
    public BigDecimal somarQuantidadeDevolvida(Long itemCompraId)
            throws SQLException {

        try (Connection conexao = DatabaseConnection.getConnection()) {
            return somarQuantidadeDevolvida(conexao, itemCompraId);
        }
    }

    // =========================
    // CONSULTAS
    // =========================

    public Optional<Devolucao> buscarPorId(Long id) throws SQLException {

        try (Connection conexao = DatabaseConnection.getConnection()) {
            return buscarPorId(conexao, id);
        }
    }

    // Versão para uso dentro de um Command (mesma transação)
    public Optional<Devolucao> buscarPorId(Connection conexao, Long id)
            throws SQLException {

        List<Devolucao> devolucoes = consultar(
            conexao, SELECT_DEVOLUCAO + " WHERE d.id = ?", id
        );

        return devolucoes.stream().findFirst();
    }

    // Lista da devolução mais recente para a mais antiga, já com os itens
    public List<Devolucao> listarTodos() throws SQLException {

        try (Connection conexao = DatabaseConnection.getConnection()) {
            return consultar(
                conexao, SELECT_DEVOLUCAO + " ORDER BY d.id DESC", null
            );
        }
    }

    // Devoluções de uma compra, da mais recente para a mais antiga
    public List<Devolucao> listarPorCompra(Long compraId)
            throws SQLException {

        try (Connection conexao = DatabaseConnection.getConnection()) {
            return consultar(
                conexao,
                SELECT_DEVOLUCAO + " WHERE d.compra_id = ? ORDER BY d.id DESC",
                compraId
            );
        }
    }

    // =========================
    // MAPEAMENTO
    // =========================

    private List<Devolucao> consultar(
            Connection conexao,
            String sql,
            Long parametro) throws SQLException {

        List<Devolucao> devolucoes = new ArrayList<>();

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            if (parametro != null) {
                comando.setLong(1, parametro);
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    devolucoes.add(mapearDevolucao(resultado));
                }
            }
        }

        carregarItens(conexao, devolucoes);

        return devolucoes;
    }

    // Busca os itens de várias devoluções em UMA consulta (evita N+1)
    private void carregarItens(Connection conexao, List<Devolucao> devolucoes)
            throws SQLException {

        if (devolucoes.isEmpty()) {
            return;
        }

        Map<Long, Devolucao> devolucoesPorId = new LinkedHashMap<>();
        for (Devolucao devolucao : devolucoes) {
            devolucoesPorId.put(devolucao.getId(), devolucao);
        }

        String marcadores = String.join(
            ", ", Collections.nCopies(devolucoes.size(), "?")
        );

        String sql = SELECT_ITENS
            + " WHERE i.devolucao_id IN (" + marcadores + ") ORDER BY i.id";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            int posicao = 1;
            for (Long devolucaoId : devolucoesPorId.keySet()) {
                comando.setLong(posicao++, devolucaoId);
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    ItemDevolucao item = mapearItem(resultado);
                    devolucoesPorId.get(item.getDevolucaoId())
                        .carregarItem(item);
                }
            }
        }
    }

    private Devolucao mapearDevolucao(ResultSet resultado)
            throws SQLException {

        Fornecedor fornecedor = new Fornecedor(
            resultado.getLong("f_id"),
            resultado.getString("f_cnpj"),
            resultado.getString("f_razao_social"),
            resultado.getString("f_nome_fantasia"),
            resultado.getString("f_inscricao_estadual"),
            resultado.getString("f_telefone"),
            resultado.getString("f_email"),
            resultado.getString("f_cep"),
            resultado.getString("f_logradouro"),
            resultado.getString("f_numero"),
            resultado.getString("f_complemento"),
            resultado.getString("f_bairro"),
            resultado.getString("f_municipio"),
            resultado.getString("f_uf"),
            resultado.getString("f_observacoes"),
            resultado.getBoolean("f_ativo")
        );

        // Compra sem itens: a devolução só precisa identificá-la
        Compra compra = new Compra(
            resultado.getLong("c_id"),
            fornecedor,
            resultado.getDate("c_data_compra").toLocalDate(),
            resultado.getBigDecimal("c_frete"),
            resultado.getBigDecimal("c_desconto"),
            StatusCompra.valueOf(resultado.getString("c_status"))
        );

        return new Devolucao(
            resultado.getLong("d_id"),
            compra,
            resultado.getDate("d_data_devolucao").toLocalDate(),
            MotivoDevolucao.valueOf(resultado.getString("d_motivo")),
            resultado.getString("d_descricao"),
            StatusDevolucao.valueOf(resultado.getString("d_status"))
        );
    }

    private ItemDevolucao mapearItem(ResultSet resultado) throws SQLException {

        Produto produto = new Produto(
            resultado.getLong("p_id"),
            resultado.getString("p_codigo"),
            resultado.getString("p_nome"),
            resultado.getString("p_descricao"),
            resultado.getString("p_unidade_medida"),
            resultado.getBigDecimal("p_quantidade_estoque"),
            resultado.getBigDecimal("p_quantidade_minima"),
            resultado.getBigDecimal("p_preco_venda"),
            resultado.getBoolean("p_ativo")
        );

        ItemCompra itemCompra = new ItemCompra(
            resultado.getLong("ic_id"),
            resultado.getLong("ic_compra_id"),
            produto,
            resultado.getBigDecimal("ic_quantidade"),
            resultado.getBigDecimal("ic_preco_unitario")
        );

        return new ItemDevolucao(
            resultado.getLong("i_id"),
            resultado.getLong("i_devolucao_id"),
            itemCompra,
            resultado.getBigDecimal("i_quantidade")
        );
    }
}
