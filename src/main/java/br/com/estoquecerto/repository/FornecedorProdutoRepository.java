
package br.com.estoquecerto.repository;

import br.com.estoquecerto.infrastructure.database.DatabaseConnection;
import br.com.estoquecerto.model.entity.Fornecedor;
import br.com.estoquecerto.model.entity.FornecedorProduto;
import br.com.estoquecerto.model.entity.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FornecedorProdutoRepository {

    public Long salvar(FornecedorProduto fornecedorProduto)
            throws SQLException {

        String sql = """
            INSERT INTO fornecedor_produto (
                fornecedor_id,
                produto_id
            ) VALUES (?, ?)
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setLong(1, fornecedorProduto.getFornecedor().getId());
            comando.setLong(2, fornecedorProduto.getProduto().getId());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getLong(1);
                }
            }
        }

        throw new SQLException("Não foi possível obter o ID da associação.");
    }

    public Optional<FornecedorProduto> buscarPorId(Long id)
            throws SQLException {

        String sql = """
            SELECT
                fp.id AS fp_id,

                f.id AS f_id,
                f.cnpj AS f_cnpj,
                f.razao_social AS f_razao_social,
                f.nome_fantasia AS f_nome_fantasia,
                f.inscricao_estadual AS f_inscricao_estadual,
                f.telefone AS f_telefone,
                f.email AS f_email,
                f.cep AS f_cep,
                f.logradouro AS f_logradouro,
                f.numero AS f_numero,
                f.complemento AS f_complemento,
                f.bairro AS f_bairro,
                f.municipio AS f_municipio,
                f.uf AS f_uf,
                f.observacoes AS f_observacoes,
                f.ativo AS f_ativo,

                p.id AS p_id,
                p.codigo AS p_codigo,
                p.nome AS p_nome,
                p.descricao AS p_descricao,
                p.unidade_medida AS p_unidade_medida,
                p.quantidade_estoque AS p_quantidade_estoque,
                p.quantidade_minima AS p_quantidade_minima,
                p.preco_venda AS p_preco_venda,
                p.ativo AS p_ativo

            FROM fornecedor_produto fp

            INNER JOIN fornecedores f
                ON fp.fornecedor_id = f.id

            INNER JOIN produtos p
                ON fp.produto_id = p.id

            WHERE fp.id = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return Optional.of(mapearFornecedorProduto(resultado));
                }
            }
        }

        return Optional.empty();
    }

    public List<FornecedorProduto> listarTodos() throws SQLException {

        String sql = """
            SELECT
                fp.id AS fp_id,

                f.id AS f_id,
                f.cnpj AS f_cnpj,
                f.razao_social AS f_razao_social,
                f.nome_fantasia AS f_nome_fantasia,
                f.inscricao_estadual AS f_inscricao_estadual,
                f.telefone AS f_telefone,
                f.email AS f_email,
                f.cep AS f_cep,
                f.logradouro AS f_logradouro,
                f.numero AS f_numero,
                f.complemento AS f_complemento,
                f.bairro AS f_bairro,
                f.municipio AS f_municipio,
                f.uf AS f_uf,
                f.observacoes AS f_observacoes,
                f.ativo AS f_ativo,

                p.id AS p_id,
                p.codigo AS p_codigo,
                p.nome AS p_nome,
                p.descricao AS p_descricao,
                p.unidade_medida AS p_unidade_medida,
                p.quantidade_estoque AS p_quantidade_estoque,
                p.quantidade_minima AS p_quantidade_minima,
                p.preco_venda AS p_preco_venda,
                p.ativo AS p_ativo

            FROM fornecedor_produto fp

            INNER JOIN fornecedores f
                ON fp.fornecedor_id = f.id

            INNER JOIN produtos p
                ON fp.produto_id = p.id

            ORDER BY fp.id
            """;

        List<FornecedorProduto> associacoes = new ArrayList<>();

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                associacoes.add(mapearFornecedorProduto(resultado));
            }
        }

        return associacoes;
    }

    public List<FornecedorProduto> listarPorFornecedor(Long fornecedorId)
            throws SQLException {

        String sql = """
            SELECT
                fp.id AS fp_id,

                f.id AS f_id,
                f.cnpj AS f_cnpj,
                f.razao_social AS f_razao_social,
                f.nome_fantasia AS f_nome_fantasia,
                f.inscricao_estadual AS f_inscricao_estadual,
                f.telefone AS f_telefone,
                f.email AS f_email,
                f.cep AS f_cep,
                f.logradouro AS f_logradouro,
                f.numero AS f_numero,
                f.complemento AS f_complemento,
                f.bairro AS f_bairro,
                f.municipio AS f_municipio,
                f.uf AS f_uf,
                f.observacoes AS f_observacoes,
                f.ativo AS f_ativo,

                p.id AS p_id,
                p.codigo AS p_codigo,
                p.nome AS p_nome,
                p.descricao AS p_descricao,
                p.unidade_medida AS p_unidade_medida,
                p.quantidade_estoque AS p_quantidade_estoque,
                p.quantidade_minima AS p_quantidade_minima,
                p.preco_venda AS p_preco_venda,
                p.ativo AS p_ativo

            FROM fornecedor_produto fp

            INNER JOIN fornecedores f
                ON fp.fornecedor_id = f.id

            INNER JOIN produtos p
                ON fp.produto_id = p.id

            WHERE fp.fornecedor_id = ?

            ORDER BY fp.id
            """;

        List<FornecedorProduto> associacoes = new ArrayList<>();

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, fornecedorId);

            try (ResultSet resultado = comando.executeQuery()) {

                while (resultado.next()) {
                    associacoes.add(mapearFornecedorProduto(resultado));
                }
            }
        }

        return associacoes;
    }

    public boolean excluir(Long id) throws SQLException {

        String sql = """
            DELETE FROM fornecedor_produto
            WHERE id = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            return comando.executeUpdate() > 0;
        }
    }

    private FornecedorProduto mapearFornecedorProduto(
            ResultSet resultado) throws SQLException {

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

        return new FornecedorProduto(
            resultado.getLong("fp_id"),
            fornecedor,
            produto
        );
    }


    public boolean existeAssociacao(Long fornecedorId, Long produtoId)
            throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM fornecedor_produto
            WHERE fornecedor_id = ?
            AND produto_id = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, fornecedorId);
            comando.setLong(2, produtoId);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getInt(1) > 0;
                }
            }
        }

        return false;
    }
}
