package br.com.estoquecerto.repository;

import br.com.estoquecerto.infrastructure.database.DatabaseConnection;
import br.com.estoquecerto.model.entity.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class ProdutoRepository {

    public Long salvar(Produto produto) throws SQLException {

        String sql = """
            INSERT INTO produtos (
                codigo,
                nome,
                descricao,
                unidade_medida,
                quantidade_estoque,
                quantidade_minima,
                preco_venda,
                ativo
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setString(1, produto.getCodigo());
            comando.setString(2, produto.getNome());
            comando.setString(3, produto.getDescricao());
            comando.setString(4, produto.getUnidadeMedida());
            comando.setBigDecimal(5, produto.getQuantidadeEstoque());
            comando.setBigDecimal(6, produto.getQuantidadeMinima());
            comando.setBigDecimal(7, produto.getPrecoVenda());
            comando.setBoolean(8, produto.isAtivo());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getLong(1);
                }
            }
        }

        throw new SQLException("Não foi possível obter o ID do produto cadastrado.");
    }


    public Optional<Produto> buscarPorId(Long id) throws SQLException {

        String sql = """
            SELECT
                id,
                codigo,
                nome,
                descricao,
                unidade_medida,
                quantidade_estoque,
                quantidade_minima,
                preco_venda,
                ativo
            FROM produtos
            WHERE id = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {

                    Produto produto = new Produto(
                        resultado.getLong("id"),
                        resultado.getString("codigo"),
                        resultado.getString("nome"),
                        resultado.getString("descricao"),
                        resultado.getString("unidade_medida"),
                        resultado.getBigDecimal("quantidade_estoque"),
                        resultado.getBigDecimal("quantidade_minima"),
                        resultado.getBigDecimal("preco_venda"),
                        resultado.getBoolean("ativo")
                    );

                    return Optional.of(produto);
                }
            }
        }

        return Optional.empty();
    }

   
    public List<Produto> listarTodos() throws SQLException {

        String sql = """
            SELECT
                id,
                codigo,
                nome,
                descricao,
                unidade_medida,
                quantidade_estoque,
                quantidade_minima,
                preco_venda,
                ativo
            FROM produtos
            """;

        List<Produto> produtos = new ArrayList<>();

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {

                Produto produto = new Produto(
                    resultado.getLong("id"),
                    resultado.getString("codigo"),
                    resultado.getString("nome"),
                    resultado.getString("descricao"),
                    resultado.getString("unidade_medida"),
                    resultado.getBigDecimal("quantidade_estoque"),
                    resultado.getBigDecimal("quantidade_minima"),
                    resultado.getBigDecimal("preco_venda"),
                    resultado.getBoolean("ativo")
                );

                produtos.add(produto);
            }
        }

        return produtos;
    }

    public Optional<Produto> buscarPorCodigo(String codigo) throws SQLException {

        String sql = """
            SELECT
                id,
                codigo,
                nome,
                descricao,
                unidade_medida,
                quantidade_estoque,
                quantidade_minima,
                preco_venda,
                ativo
            FROM produtos
            WHERE codigo = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, codigo);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {

                    Produto produto = new Produto(
                        resultado.getLong("id"),
                        resultado.getString("codigo"),
                        resultado.getString("nome"),
                        resultado.getString("descricao"),
                        resultado.getString("unidade_medida"),
                        resultado.getBigDecimal("quantidade_estoque"),
                        resultado.getBigDecimal("quantidade_minima"),
                        resultado.getBigDecimal("preco_venda"),
                        resultado.getBoolean("ativo")
                    );

                    return Optional.of(produto);
                }
            }
        }

        return Optional.empty();
    }

    public boolean atualizar(Long id, Produto produto) throws SQLException {

        String sql = """
            UPDATE produtos
            SET
                nome = ?,
                descricao = ?,
                unidade_medida = ?,
                quantidade_minima = ?,
                preco_venda = ?
            WHERE id = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, produto.getNome());
            comando.setString(2, produto.getDescricao());
            comando.setString(3, produto.getUnidadeMedida());
            comando.setBigDecimal(4, produto.getQuantidadeMinima());
            comando.setBigDecimal(5, produto.getPrecoVenda());
            comando.setLong(6, id);

            return comando.executeUpdate() > 0;
        }
    }

    public boolean desativar(Long id) throws SQLException {

        String sql = """
            UPDATE produtos
            SET ativo = false
            WHERE id = ? AND ativo = true
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            return comando.executeUpdate() > 0;
        }
    }
}

