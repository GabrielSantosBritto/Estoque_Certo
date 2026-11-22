package br.com.estoquecerto.repository;

import br.com.estoquecerto.infrastructure.database.DatabaseConnection;
import br.com.estoquecerto.model.entity.Compra;
import br.com.estoquecerto.model.entity.Fornecedor;
import br.com.estoquecerto.model.entity.ItemCompra;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.StatusCompra;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistência das compras e de seus itens.
 *
 * Métodos que recebem Connection participam da transação de um Command
 * (não abrem, não fecham e não fazem commit). Os demais são consultas
 * avulsas e abrem a própria conexão.
 */
public class CompraRepository {

    private static final String SELECT_COMPRA = """
        SELECT
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

        FROM compras c
        INNER JOIN fornecedores f ON f.id = c.fornecedor_id
        """;

    private static final String SELECT_ITENS = """
        SELECT
            i.id                  AS i_id,
            i.compra_id           AS i_compra_id,
            i.quantidade          AS i_quantidade,
            i.preco_unitario      AS i_preco_unitario,

            p.id                  AS p_id,
            p.codigo              AS p_codigo,
            p.nome                AS p_nome,
            p.descricao           AS p_descricao,
            p.unidade_medida      AS p_unidade_medida,
            p.quantidade_estoque  AS p_quantidade_estoque,
            p.quantidade_minima   AS p_quantidade_minima,
            p.preco_venda         AS p_preco_venda,
            p.ativo               AS p_ativo

        FROM itens_compra i
        INNER JOIN produtos p ON p.id = i.produto_id
        """;

    // =========================
    // GRAVAÇÃO (dentro de transação)
    // =========================

    // Grava a compra e todos os itens. Retorna o ID da compra.
    public Long salvar(Connection conexao, Compra compra)
            throws SQLException {

        String sqlCompra = """
            INSERT INTO compras (
                fornecedor_id,
                data_compra,
                frete,
                desconto,
                status
            ) VALUES (?, ?, ?, ?, ?)
            """;

        Long compraId;

        try (PreparedStatement comando = conexao.prepareStatement(
                sqlCompra, Statement.RETURN_GENERATED_KEYS)) {

            comando.setLong(1, compra.getFornecedor().getId());
            comando.setDate(2, Date.valueOf(compra.getDataCompra()));
            comando.setBigDecimal(3, compra.getFrete());
            comando.setBigDecimal(4, compra.getDesconto());
            comando.setString(5, compra.getStatus().name());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (!resultado.next()) {
                    throw new SQLException(
                        "Não foi possível obter o ID da compra cadastrada."
                    );
                }
                compraId = resultado.getLong(1);
            }
        }

        String sqlItem = """
            INSERT INTO itens_compra (
                compra_id,
                produto_id,
                quantidade,
                preco_unitario
            ) VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement comando = conexao.prepareStatement(sqlItem)) {

            for (ItemCompra item : compra.getItens()) {
                comando.setLong(1, compraId);
                comando.setLong(2, item.getProduto().getId());
                comando.setBigDecimal(3, item.getQuantidade());
                comando.setBigDecimal(4, item.getPrecoUnitario());
                comando.addBatch();
            }

            comando.executeBatch();
        }

        return compraId;
    }

    /**
     * Troca o status somente se a compra ainda estiver no status esperado.
     *
     * A condição no WHERE torna a troca atômica: se duas requisições
     * tentarem receber a mesma compra ao mesmo tempo, apenas uma altera
     * a linha; a outra recebe false.
     */
    public boolean atualizarStatus(
            Connection conexao,
            Long compraId,
            StatusCompra statusEsperado,
            StatusCompra novoStatus) throws SQLException {

        String sql = """
            UPDATE compras
            SET status = ?
            WHERE id = ? AND status = ?
            """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, novoStatus.name());
            comando.setLong(2, compraId);
            comando.setString(3, statusEsperado.name());

            return comando.executeUpdate() > 0;
        }
    }

    // =========================
    // CONSULTAS
    // =========================

    public Optional<Compra> buscarPorId(Long id) throws SQLException {

        try (Connection conexao = DatabaseConnection.getConnection()) {
            return buscarPorId(conexao, id);
        }
    }

    // Versão para uso dentro de um Command (mesma transação)
    public Optional<Compra> buscarPorId(Connection conexao, Long id)
            throws SQLException {

        String sql = SELECT_COMPRA + " WHERE c.id = ?";

        Compra compra = null;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    compra = mapearCompra(resultado);
                }
            }
        }

        if (compra == null) {
            return Optional.empty();
        }

        carregarItens(conexao, List.of(compra));

        return Optional.of(compra);
    }

    // Lista da compra mais recente para a mais antiga, já com os itens
    public List<Compra> listarTodos() throws SQLException {

        String sql = SELECT_COMPRA + " ORDER BY c.id DESC";

        List<Compra> compras = new ArrayList<>();

        try (Connection conexao = DatabaseConnection.getConnection()) {

            try (PreparedStatement comando = conexao.prepareStatement(sql);
                 ResultSet resultado = comando.executeQuery()) {

                while (resultado.next()) {
                    compras.add(mapearCompra(resultado));
                }
            }

            carregarItens(conexao, compras);
        }

        return compras;
    }

    // =========================
    // MAPEAMENTO
    // =========================

    // Busca os itens de várias compras em UMA consulta (evita N+1)
    private void carregarItens(Connection conexao, List<Compra> compras)
            throws SQLException {

        if (compras.isEmpty()) {
            return;
        }

        Map<Long, Compra> comprasPorId = new LinkedHashMap<>();
        for (Compra compra : compras) {
            comprasPorId.put(compra.getId(), compra);
        }

        String marcadores = String.join(
            ", ", Collections.nCopies(compras.size(), "?")
        );

        String sql = SELECT_ITENS
            + " WHERE i.compra_id IN (" + marcadores + ") ORDER BY i.id";

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            int posicao = 1;
            for (Long compraId : comprasPorId.keySet()) {
                comando.setLong(posicao++, compraId);
            }

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    ItemCompra item = mapearItem(resultado);
                    comprasPorId.get(item.getCompraId()).carregarItem(item);
                }
            }
        }
    }

    private Compra mapearCompra(ResultSet resultado) throws SQLException {

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

        return new Compra(
            resultado.getLong("c_id"),
            fornecedor,
            resultado.getDate("c_data_compra").toLocalDate(),
            resultado.getBigDecimal("c_frete"),
            resultado.getBigDecimal("c_desconto"),
            StatusCompra.valueOf(resultado.getString("c_status"))
        );
    }

    private ItemCompra mapearItem(ResultSet resultado) throws SQLException {

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

        return new ItemCompra(
            resultado.getLong("i_id"),
            resultado.getLong("i_compra_id"),
            produto,
            resultado.getBigDecimal("i_quantidade"),
            resultado.getBigDecimal("i_preco_unitario")
        );
    }
}
