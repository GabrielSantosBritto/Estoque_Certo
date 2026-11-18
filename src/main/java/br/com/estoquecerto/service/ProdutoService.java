package br.com.estoquecerto.service;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

public class ProdutoService {

    private static final int TENTATIVAS_GERAR_CODIGO = 3;

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

        validarValores(produto);

        // O código (SKU) é gerado pelo sistema no formato PROD-0001.
        // Um código enviado na requisição é ignorado.
        for (int tentativa = 1; tentativa <= TENTATIVAS_GERAR_CODIGO;
                tentativa++) {

            Produto novoProduto = new Produto(
                produtoRepository.gerarProximoCodigo(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getUnidadeMedida(),
                produto.getQuantidadeMinima(),
                produto.getPrecoVenda()
            );

            try {
                return produtoRepository.salvar(novoProduto);

            } catch (SQLIntegrityConstraintViolationException e) {
                // Outro cadastro simultâneo usou o mesmo código:
                // gera o próximo e tenta de novo.
                if (tentativa == TENTATIVAS_GERAR_CODIGO) {
                    throw e;
                }
            }
        }

        throw new IllegalStateException(
            "Não foi possível gerar o código do produto."
        );
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

        validarValores(produto);

        Produto existente = produtoRepository.buscarPorId(id)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("Produto não encontrado.")
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
                    new RecursoNaoEncontradoException("Produto não encontrado.")
                );

        if (!produto.isAtivo()) {
            throw new ConflitoException(
                "O produto já está desativado."
            );
        }

        if (!produtoRepository.desativar(id)) {
            throw new IllegalArgumentException(
                "Não foi possível desativar o produto."
            );
        }
    }

    // Valores numéricos do produto
    private void validarValores(Produto produto) {

        if (produto.getQuantidadeMinima() == null) {
            throw new IllegalArgumentException(
                "A quantidade mínima do produto é obrigatória."
            );
        }

        if (produto.getQuantidadeMinima().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "A quantidade mínima não pode ser negativa."
            );
        }

        if (produto.getPrecoVenda() != null &&
            produto.getPrecoVenda().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "O preço de venda não pode ser negativo."
            );
        }
    }
}
