package br.com.estoquecerto.service;

import br.com.estoquecerto.command.CadastrarCompraCommand;
import br.com.estoquecerto.command.CancelarCompraCommand;
import br.com.estoquecerto.command.ConfirmarRecebimentoCommand;
import br.com.estoquecerto.command.ExecutorTransacional;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.model.entity.Compra;
import br.com.estoquecerto.model.entity.Fornecedor;
import br.com.estoquecerto.model.entity.ItemCompra;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.repository.CompraRepository;
import br.com.estoquecerto.repository.FornecedorProdutoRepository;
import br.com.estoquecerto.repository.FornecedorRepository;
import br.com.estoquecerto.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio das compras.
 *
 * As operações que gravam (cadastrar, cancelar e, na próxima etapa,
 * receber) são executadas como Commands pelo ExecutorTransacional.
 */
public class CompraService {

    // Dados de um item recebidos na criação da compra
    public record NovoItem(
            Long produtoId,
            BigDecimal quantidade,
            BigDecimal precoUnitario) {
    }

    private final CompraRepository compraRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;
    private final FornecedorProdutoRepository fornecedorProdutoRepository;
    private final ExecutorTransacional executorTransacional;

    public CompraService() {
        this.compraRepository = new CompraRepository();
        this.fornecedorRepository = new FornecedorRepository();
        this.produtoRepository = new ProdutoRepository();
        this.fornecedorProdutoRepository = new FornecedorProdutoRepository();
        this.executorTransacional = new ExecutorTransacional();
    }

    // Cadastrar compra (status inicial PENDENTE)
    public Long cadastrar(
            Long fornecedorId,
            LocalDate dataCompra,
            BigDecimal frete,
            BigDecimal desconto,
            List<NovoItem> itens) throws SQLException {

        validarId(fornecedorId, "ID do fornecedor inválido.");

        if (dataCompra != null && dataCompra.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "A data da compra não pode estar no futuro."
            );
        }

        validarNaoNegativo(frete, "O frete não pode ser negativo.");
        validarNaoNegativo(desconto, "O desconto não pode ser negativo.");

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException(
                "A compra precisa ter pelo menos um item."
            );
        }

        Fornecedor fornecedor = fornecedorRepository.buscarPorId(fornecedorId)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("Fornecedor não encontrado.")
                );

        if (!fornecedor.isAtivo()) {
            throw new IllegalArgumentException(
                "Não é possível registrar compra de um fornecedor inativo."
            );
        }

        Compra compra = new Compra(fornecedor, dataCompra, frete, desconto);

        for (NovoItem novoItem : itens) {
            compra.adicionarItem(montarItem(fornecedor, novoItem));
        }

        if (compra.getTotal().signum() < 0) {
            throw new IllegalArgumentException(
                "O desconto não pode ser maior que o valor dos itens mais o frete."
            );
        }

        CadastrarCompraCommand comando = new CadastrarCompraCommand(compra);

        executorTransacional.executar(comando);

        return comando.getCompraId();
    }

    // Buscar compra pelo ID, com itens e totais
    public Optional<Compra> buscarPorId(Long id) throws SQLException {

        validarId(id, "ID da compra inválido.");

        return compraRepository.buscarPorId(id);
    }

    // Listar todas as compras
    public List<Compra> listarTodos() throws SQLException {

        return compraRepository.listarTodos();
    }

    // Cancelar compra pendente
    public void cancelar(Long id) throws SQLException {

        validarId(id, "ID da compra inválido.");

        executorTransacional.executar(new CancelarCompraCommand(id));
    }

    // Confirmar recebimento: dá entrada de todos os itens no estoque.
    // Retorna quantos itens foram recebidos.
    public int receber(Long id) throws SQLException {

        validarId(id, "ID da compra inválido.");

        ConfirmarRecebimentoCommand comando =
            new ConfirmarRecebimentoCommand(id);

        executorTransacional.executar(comando);

        return comando.getItensRecebidos();
    }

    // Valida um item e monta o ItemCompra com o produto do banco
    private ItemCompra montarItem(Fornecedor fornecedor, NovoItem novoItem)
            throws SQLException {

        if (novoItem == null) {
            throw new IllegalArgumentException("Item da compra inválido.");
        }

        validarId(novoItem.produtoId(), "ID do produto inválido.");
        validarPositivo(novoItem.quantidade(),
            "A quantidade de cada item deve ser maior que zero.");
        validarPositivo(novoItem.precoUnitario(),
            "O preço unitário de cada item deve ser maior que zero.");

        Produto produto = produtoRepository.buscarPorId(novoItem.produtoId())
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException(
                        "Produto " + novoItem.produtoId() + " não encontrado."
                    )
                );

        if (!produto.isAtivo()) {
            throw new IllegalArgumentException(
                "O produto " + produto.getCodigo() + " está inativo."
            );
        }

        // Só entra na compra o que o fornecedor está cadastrado para fornecer
        if (!fornecedorProdutoRepository.existeAssociacao(
                fornecedor.getId(), produto.getId())) {

            throw new IllegalArgumentException(
                "O produto " + produto.getCodigo()
                + " não está associado ao fornecedor "
                + fornecedor.getRazaoSocial() + "."
            );
        }

        return new ItemCompra(
            produto,
            novoItem.quantidade(),
            novoItem.precoUnitario()
        );
    }

    private void validarId(Long id, String mensagem) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private void validarPositivo(BigDecimal valor, String mensagem) {

        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private void validarNaoNegativo(BigDecimal valor, String mensagem) {

        if (valor != null && valor.signum() < 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
