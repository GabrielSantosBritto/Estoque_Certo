package br.com.estoquecerto.repository;

import br.com.estoquecerto.infrastructure.database.DatabaseConnection;
import br.com.estoquecerto.model.entity.Fornecedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class FornecedorRepository {

    // Cadastrar fornecedor
    public Long salvar(Fornecedor fornecedor) throws SQLException {

        String sql = """
            INSERT INTO fornecedores (
                cnpj,
                razao_social,
                nome_fantasia,
                inscricao_estadual,
                telefone,
                email,
                cep,
                logradouro,
                numero,
                complemento,
                bairro,
                municipio,
                uf,
                observacoes,
                ativo
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setString(1, fornecedor.getCnpj());
            comando.setString(2, fornecedor.getRazaoSocial());
            comando.setString(3, fornecedor.getNomeFantasia());
            comando.setString(4, fornecedor.getInscricaoEstadual());
            comando.setString(5, fornecedor.getTelefone());
            comando.setString(6, fornecedor.getEmail());
            comando.setString(7, fornecedor.getCep());
            comando.setString(8, fornecedor.getLogradouro());
            comando.setString(9, fornecedor.getNumero());
            comando.setString(10, fornecedor.getComplemento());
            comando.setString(11, fornecedor.getBairro());
            comando.setString(12, fornecedor.getMunicipio());
            comando.setString(13, fornecedor.getUf());
            comando.setString(14, fornecedor.getObservacoes());
            comando.setBoolean(15, fornecedor.isAtivo());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getLong(1);
                }
            }
        }

        throw new SQLException(
            "Não foi possível obter o ID do fornecedor cadastrado."
        );
    }

    // Buscar fornecedor pelo ID
    public Optional<Fornecedor> buscarPorId(Long id)
            throws SQLException {

        String sql = """
            SELECT *
            FROM fornecedores
            WHERE id = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return Optional.of(mapearFornecedor(resultado));
                }
            }
        }

        return Optional.empty();
    }

    // Buscar fornecedor pelo CNPJ
    public Optional<Fornecedor> buscarPorCnpj(String cnpj)
            throws SQLException {

        String sql = """
            SELECT *
            FROM fornecedores
            WHERE cnpj = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, cnpj);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return Optional.of(mapearFornecedor(resultado));
                }
            }
        }

        return Optional.empty();
    }

    // Listar todos os fornecedores
    public List<Fornecedor> listarTodos() throws SQLException {

        String sql = """
            SELECT *
            FROM fornecedores
            ORDER BY id
            """;

        List<Fornecedor> fornecedores = new ArrayList<>();

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                fornecedores.add(mapearFornecedor(resultado));
            }
        }

        return fornecedores;
    }

    // Atualizar dados cadastrais
    public boolean atualizar(Long id, Fornecedor fornecedor)
            throws SQLException {

        String sql = """
            UPDATE fornecedores
            SET
                razao_social = ?,
                nome_fantasia = ?,
                inscricao_estadual = ?,
                telefone = ?,
                email = ?,
                cep = ?,
                logradouro = ?,
                numero = ?,
                complemento = ?,
                bairro = ?,
                municipio = ?,
                uf = ?,
                observacoes = ?
            WHERE id = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, fornecedor.getRazaoSocial());
            comando.setString(2, fornecedor.getNomeFantasia());
            comando.setString(3, fornecedor.getInscricaoEstadual());
            comando.setString(4, fornecedor.getTelefone());
            comando.setString(5, fornecedor.getEmail());
            comando.setString(6, fornecedor.getCep());
            comando.setString(7, fornecedor.getLogradouro());
            comando.setString(8, fornecedor.getNumero());
            comando.setString(9, fornecedor.getComplemento());
            comando.setString(10, fornecedor.getBairro());
            comando.setString(11, fornecedor.getMunicipio());
            comando.setString(12, fornecedor.getUf());
            comando.setString(13, fornecedor.getObservacoes());
            comando.setLong(14, id);

            return comando.executeUpdate() > 0;
        }
    }

    // Desativar fornecedor
    public boolean desativar(Long id) throws SQLException {

        String sql = """
            UPDATE fornecedores
            SET ativo = false
            WHERE id = ? AND ativo = true
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            return comando.executeUpdate() > 0;
        }
    }

    // Converter resultado do banco em entidade
    private Fornecedor mapearFornecedor(ResultSet resultado)
            throws SQLException {

        return new Fornecedor(
            resultado.getLong("id"),
            resultado.getString("cnpj"),
            resultado.getString("razao_social"),
            resultado.getString("nome_fantasia"),
            resultado.getString("inscricao_estadual"),
            resultado.getString("telefone"),
            resultado.getString("email"),
            resultado.getString("cep"),
            resultado.getString("logradouro"),
            resultado.getString("numero"),
            resultado.getString("complemento"),
            resultado.getString("bairro"),
            resultado.getString("municipio"),
            resultado.getString("uf"),
            resultado.getString("observacoes"),
            resultado.getBoolean("ativo")
        );
    }
}

