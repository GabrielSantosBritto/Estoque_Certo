package br.com.estoquecerto.service;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.model.entity.Fornecedor;
import br.com.estoquecerto.model.entity.FornecedorProduto;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.repository.FornecedorProdutoRepository;
import br.com.estoquecerto.repository.FornecedorRepository;
import br.com.estoquecerto.repository.ProdutoRepository;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

public class FornecedorProdutoService {

    private final FornecedorProdutoRepository fornecedorProdutoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;

    public FornecedorProdutoService() {
        this.fornecedorProdutoRepository = new FornecedorProdutoRepository();
        this.fornecedorRepository = new FornecedorRepository();
        this.produtoRepository = new ProdutoRepository();
    }

    // Associar produto a fornecedor
    public Long associar(Long fornecedorId, Long produtoId)
            throws SQLException {

        validarId(fornecedorId, "ID do fornecedor inválido.");
        validarId(produtoId, "ID do produto inválido.");

        Fornecedor fornecedor = fornecedorRepository.buscarPorId(fornecedorId)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("Fornecedor não encontrado.")
                );

        if (!fornecedor.isAtivo()) {
            throw new IllegalArgumentException(
                "Não é possível associar um fornecedor inativo."
            );
        }

        Produto produto = produtoRepository.buscarPorId(produtoId)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("Produto não encontrado.")
                );

        if (!produto.isAtivo()) {
            throw new IllegalArgumentException(
                "Não é possível associar um produto inativo."
            );
        }

        if (fornecedorProdutoRepository.existeAssociacao(
                fornecedorId, produtoId)) {
            throw new ConflitoException(
                "Este produto já está associado a este fornecedor."
            );
        }

        try {
            return fornecedorProdutoRepository.salvar(
                new FornecedorProduto(fornecedor, produto)
            );

        } catch (SQLIntegrityConstraintViolationException e) {
            // Proteção extra: a UNIQUE (fornecedor_id, produto_id) do banco
            // barra duas requisições simultâneas que passaram pela checagem.
            throw new ConflitoException(
                "Este produto já está associado a este fornecedor."
            );
        }
    }

    // Buscar associação pelo ID
    public Optional<FornecedorProduto> buscarPorId(Long id)
            throws SQLException {

        validarId(id, "ID da associação inválido.");

        return fornecedorProdutoRepository.buscarPorId(id);
    }

    // Listar todas as associações
    public List<FornecedorProduto> listarTodos() throws SQLException {

        return fornecedorProdutoRepository.listarTodos();
    }

    // Listar produtos de um fornecedor
    public List<FornecedorProduto> listarPorFornecedor(Long fornecedorId)
            throws SQLException {

        validarId(fornecedorId, "ID do fornecedor inválido.");

        if (fornecedorRepository.buscarPorId(fornecedorId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Fornecedor não encontrado.");
        }

        return fornecedorProdutoRepository.listarPorFornecedor(fornecedorId);
    }

    // Excluir associação
    public void excluir(Long id) throws SQLException {

        validarId(id, "ID da associação inválido.");

        if (!fornecedorProdutoRepository.excluir(id)) {
            throw new RecursoNaoEncontradoException("Associação não encontrada.");
        }
    }

    private void validarId(Long id, String mensagem) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
