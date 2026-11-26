package br.com.estoquecerto.service;

import br.com.estoquecerto.command.CadastrarDevolucaoCommand;
import br.com.estoquecerto.command.CancelarDevolucaoCommand;
import br.com.estoquecerto.command.ConcluirDevolucaoCommand;
import br.com.estoquecerto.command.ExecutorTransacional;
import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.model.entity.Compra;
import br.com.estoquecerto.model.entity.Devolucao;
import br.com.estoquecerto.model.entity.ItemCompra;
import br.com.estoquecerto.model.entity.ItemDevolucao;
import br.com.estoquecerto.model.enums.MotivoDevolucao;
import br.com.estoquecerto.model.enums.StatusCompra;
import br.com.estoquecerto.repository.CompraRepository;
import br.com.estoquecerto.repository.DevolucaoRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Regras de negócio das devoluções ao fornecedor.
 *
 * As operações que gravam (cadastrar, concluir e cancelar) são
 * executadas como Commands pelo ExecutorTransacional.
 */
public class DevolucaoService {

    private static final int TAMANHO_MAXIMO_DESCRICAO = 500;

    // Dados de um item recebidos na criação da devolução
    public record NovoItem(Long itemCompraId, BigDecimal quantidade) {
    }

    private final DevolucaoRepository devolucaoRepository;
    private final CompraRepository compraRepository;
    private final ExecutorTransacional executorTransacional;

    public DevolucaoService() {
        this.devolucaoRepository = new DevolucaoRepository();
        this.compraRepository = new CompraRepository();
        this.executorTransacional = new ExecutorTransacional();
    }

    // Cadastrar devolução (status inicial PENDENTE; não mexe no estoque)
    public Long cadastrar(
            Long compraId,
            LocalDate dataDevolucao,
            String motivoTexto,
            String descricao,
            List<NovoItem> itens) throws SQLException {

        validarId(compraId, "ID da compra inválido.");

        MotivoDevolucao motivo = converterMotivo(motivoTexto);
        String descricaoTratada = validarDescricao(motivo, descricao);

        if (dataDevolucao != null && dataDevolucao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "A data da devolução não pode estar no futuro."
            );
        }

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException(
                "A devolução precisa ter pelo menos um item."
            );
        }

        Compra compra = compraRepository.buscarPorId(compraId)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("Compra não encontrada.")
                );

        // Só volta ao fornecedor o que de fato entrou no estoque
        if (compra.getStatus() != StatusCompra.RECEBIDA) {
            throw new ConflitoException(
                "Só é possível devolver itens de uma compra RECEBIDA. "
                + "Status atual: " + compra.getStatus() + "."
            );
        }

        if (dataDevolucao != null &&
                dataDevolucao.isBefore(compra.getDataCompra())) {
            throw new IllegalArgumentException(
                "A data da devolução não pode ser anterior à data da compra."
            );
        }

        Devolucao devolucao = new Devolucao(
            compra, dataDevolucao, motivo, descricaoTratada
        );

        for (NovoItem novoItem : itens) {
            devolucao.adicionarItem(montarItem(compra, novoItem));
        }

        CadastrarDevolucaoCommand comando =
            new CadastrarDevolucaoCommand(devolucao);

        executorTransacional.executar(comando);

        return comando.getDevolucaoId();
    }

    // Buscar devolução pelo ID, com itens
    public Optional<Devolucao> buscarPorId(Long id) throws SQLException {

        validarId(id, "ID da devolução inválido.");

        return devolucaoRepository.buscarPorId(id);
    }

    // Listar todas as devoluções
    public List<Devolucao> listarTodos() throws SQLException {

        return devolucaoRepository.listarTodos();
    }

    // Listar as devoluções de uma compra
    public List<Devolucao> listarPorCompra(Long compraId)
            throws SQLException {

        validarId(compraId, "ID da compra inválido.");

        if (compraRepository.buscarPorId(compraId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Compra não encontrada.");
        }

        return devolucaoRepository.listarPorCompra(compraId);
    }

    // Concluir: dá saída dos itens no estoque. Retorna quantos itens saíram.
    public int concluir(Long id) throws SQLException {

        validarId(id, "ID da devolução inválido.");

        ConcluirDevolucaoCommand comando = new ConcluirDevolucaoCommand(id);

        executorTransacional.executar(comando);

        return comando.getItensDevolvidos();
    }

    // Cancelar devolução pendente
    public void cancelar(Long id) throws SQLException {

        validarId(id, "ID da devolução inválido.");

        executorTransacional.executar(new CancelarDevolucaoCommand(id));
    }

    // Valida um item e monta o ItemDevolucao com o item da compra
    private ItemDevolucao montarItem(Compra compra, NovoItem novoItem)
            throws SQLException {

        if (novoItem == null) {
            throw new IllegalArgumentException("Item da devolução inválido.");
        }

        validarId(novoItem.itemCompraId(), "ID do item da compra inválido.");

        if (novoItem.quantidade() == null ||
                novoItem.quantidade().signum() <= 0) {
            throw new IllegalArgumentException(
                "A quantidade de cada item deve ser maior que zero."
            );
        }

        // O item precisa pertencer à compra desta devolução
        ItemCompra itemCompra = compra.getItens().stream()
                .filter(item -> item.getId().equals(novoItem.itemCompraId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "O item " + novoItem.itemCompraId()
                    + " não pertence à compra " + compra.getId() + "."
                ));

        // Conferência antecipada; a definitiva acontece na conclusão
        BigDecimal jaDevolvido =
            devolucaoRepository.somarQuantidadeDevolvida(itemCompra.getId());

        if (jaDevolvido.add(novoItem.quantidade())
                .compareTo(itemCompra.getQuantidade()) > 0) {

            throw new IllegalArgumentException(
                "A quantidade devolvida do produto "
                + itemCompra.getProduto().getCodigo()
                + " ultrapassa a quantidade comprada ("
                + itemCompra.getQuantidade().stripTrailingZeros()
                    .toPlainString()
                + "). Já devolvido: "
                + jaDevolvido.stripTrailingZeros().toPlainString() + "."
            );
        }

        return new ItemDevolucao(itemCompra, novoItem.quantidade());
    }

    private MotivoDevolucao converterMotivo(String motivoTexto) {

        String valoresAceitos = Arrays.stream(MotivoDevolucao.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        if (motivoTexto == null || motivoTexto.isBlank()) {
            throw new IllegalArgumentException(
                "O motivo da devolução é obrigatório. Use: "
                + valoresAceitos + "."
            );
        }

        try {
            return MotivoDevolucao.valueOf(motivoTexto.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Motivo da devolução inválido. Use: " + valoresAceitos + "."
            );
        }
    }

    // Descrição é opcional, mas obrigatória quando o motivo é OUTROS
    private String validarDescricao(MotivoDevolucao motivo, String descricao) {

        String tratada = (descricao == null || descricao.isBlank())
            ? null
            : descricao.strip();

        if (motivo == MotivoDevolucao.OUTROS && tratada == null) {
            throw new IllegalArgumentException(
                "Informe a descrição quando o motivo for OUTROS."
            );
        }

        if (tratada != null && tratada.length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new IllegalArgumentException(
                "A descrição deve ter no máximo "
                + TAMANHO_MAXIMO_DESCRICAO + " caracteres."
            );
        }

        return tratada;
    }

    private void validarId(Long id, String mensagem) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
