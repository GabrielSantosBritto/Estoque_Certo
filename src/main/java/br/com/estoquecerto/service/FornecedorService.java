
package br.com.estoquecerto.service;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.model.entity.Fornecedor;
import br.com.estoquecerto.repository.FornecedorRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService() {
        this.fornecedorRepository = new FornecedorRepository();
    }

    // Cadastrar fornecedor
    public Long cadastrar(Fornecedor fornecedor) throws SQLException {

        if (fornecedor == null) {
            throw new IllegalArgumentException(
                "O fornecedor não pode ser nulo."
            );
        }

        if (fornecedor.getCnpj() == null ||
            fornecedor.getCnpj().isBlank()) {
            throw new IllegalArgumentException(
                "O CNPJ do fornecedor é obrigatório."
            );
        }

        if (fornecedor.getRazaoSocial() == null ||
            fornecedor.getRazaoSocial().isBlank()) {
            throw new IllegalArgumentException(
                "A razão social do fornecedor é obrigatória."
            );
        }

        if (fornecedorRepository.buscarPorCnpj(
                fornecedor.getCnpj()).isPresent()) {
            throw new ConflitoException(
                "Já existe um fornecedor cadastrado com esse CNPJ."
            );
        }

        return fornecedorRepository.salvar(fornecedor);
    }

    // Buscar fornecedor pelo ID
    public Optional<Fornecedor> buscarPorId(Long id)
            throws SQLException {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                "ID do fornecedor inválido."
            );
        }

        return fornecedorRepository.buscarPorId(id);
    }

    // Buscar fornecedor pelo CNPJ
    public Optional<Fornecedor> buscarPorCnpj(String cnpj)
            throws SQLException {

        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException(
                "O CNPJ do fornecedor é obrigatório."
            );
        }

        return fornecedorRepository.buscarPorCnpj(cnpj);
    }

    // Listar todos os fornecedores
    public List<Fornecedor> listarTodos() throws SQLException {

        return fornecedorRepository.listarTodos();
    }

    // Atualizar fornecedor
    public void atualizar(Long id, Fornecedor fornecedor)
            throws SQLException {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                "ID do fornecedor inválido."
            );
        }

        if (fornecedor == null) {
            throw new IllegalArgumentException(
                "Fornecedor inválido."
            );
        }

        if (fornecedor.getRazaoSocial() == null ||
            fornecedor.getRazaoSocial().isBlank()) {
            throw new IllegalArgumentException(
                "A razão social do fornecedor é obrigatória."
            );
        }

        Fornecedor existente = fornecedorRepository.buscarPorId(id)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException(
                        "Fornecedor não encontrado."
                    )
                );

        if (!existente.isAtivo()) {
            throw new IllegalArgumentException(
                "Não é possível atualizar um fornecedor inativo."
            );
        }

        existente.atualizarDados(
            fornecedor.getRazaoSocial(),
            fornecedor.getNomeFantasia(),
            fornecedor.getInscricaoEstadual(),
            fornecedor.getTelefone(),
            fornecedor.getEmail(),
            fornecedor.getCep(),
            fornecedor.getLogradouro(),
            fornecedor.getNumero(),
            fornecedor.getComplemento(),
            fornecedor.getBairro(),
            fornecedor.getMunicipio(),
            fornecedor.getUf(),
            fornecedor.getObservacoes()
        );

        if (!fornecedorRepository.atualizar(id, existente)) {
            throw new IllegalArgumentException(
                "Não foi possível atualizar o fornecedor."
            );
        }
    }

    // Desativar fornecedor
    public void desativar(Long id) throws SQLException {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                "ID do fornecedor inválido."
            );
        }

        Fornecedor fornecedor = fornecedorRepository.buscarPorId(id)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException(
                        "Fornecedor não encontrado."
                    )
                );

        if (!fornecedor.isAtivo()) {
            throw new ConflitoException(
                "O fornecedor já está desativado."
            );
        }

        if (!fornecedorRepository.desativar(id)) {
            throw new IllegalArgumentException(
                "Não foi possível desativar o fornecedor."
            );
        }
    }
}

