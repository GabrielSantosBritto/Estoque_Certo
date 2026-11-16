package br.com.estoquecerto.service;

import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.repository.ProdutoRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService() {
        this.produtoRepository = new ProdutoRepository();
    }

    // Cadastrar produto
    public Long cadastrar(Produto produto) throws SQLException {

        if (produto == null) {
            throw new IllegalArgumentException(
                "O produto não pode ser nulo."
            );
        }

        if (produto.getNome() == null ||
            produto.getNome().isBlank()) {
            throw new IllegalArgumentException(
                "O nome do produto é obrigatório."
            );
        }

        if (produto.getCodigo() == null ||
            produto.getCodigo().isBlank()) {
            throw new IllegalArgumentException(
                "O código do produto é obrigatório."
            );
        }

        if (produtoRepository.buscarPorCodigo(
                produto.getCodigo()).isPresent()) {
            throw new IllegalArgumentException(
                "Já existe um produto cadastrado com esse código."
            );
        }

        return produtoRepository.salvar(produto);
    }

    // Buscar produto pelo ID
    public Optional<Produto> buscarPorId(Long id)
            throws SQLException {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                "ID do produto inválido."
            );
        }

        return produtoRepository.buscarPorId(id);
    }

    // Buscar produto pelo código
    public Optional<Produto> buscarPorCodigo(String codigo)
            throws SQLException {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                "O código do produto é obrigatório."
            );
        }

        return produtoRepository.buscarPorCodigo(codigo);
    }

    // Listar todos os produtos
    public List<Produto> listarTodos() throws SQLException {

        return produtoRepository.listarTodos();
    }


    // Atualizar produto
    public void atualizar(Long id, Produto produto)
            throws SQLException {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do produto inválido.");
        }

        if (produto == null) {
            throw new IllegalArgumentException("Produto inválido.");
        }
        
        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException(
                "O nome do produto é obrigatório."
            );
        }

        Produto existente = produtoRepository.buscarPorId(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Produto não encontrado.")
                );

        if (!existente.isAtivo()) {
            throw new IllegalArgumentException(
                "Não é possível atualizar um produto inativo."
            );
        }

        existente.atualizarDados(
            produto.getNome(),
            produto.getDescricao(),
            produto.getUnidadeMedida(),
            produto.getQuantidadeMinima(),
            produto.getPrecoVenda()
        );

        if (!produtoRepository.atualizar(id, existente)) {
            throw new IllegalArgumentException(
                "Não foi possível atualizar o produto."
            );
        }
    }

    // Desativar produto
    public void desativar(Long id) throws SQLException {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do produto inválido.");
        }

        Produto produto = produtoRepository.buscarPorId(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Produto não encontrado.")
                );

        if (!produto.isAtivo()) {
            throw new IllegalArgumentException(
                "O produto já está desativado."
            );
        }

        if (!produtoRepository.desativar(id)) {
            throw new IllegalArgumentException(
                "Não foi possível desativar o produto."
            );
        }
    }

}

