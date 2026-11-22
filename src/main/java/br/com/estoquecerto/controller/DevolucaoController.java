package br.com.estoquecerto.controller;

import br.com.estoquecerto.dto.request.DevolucaoRequestDTO;
import br.com.estoquecerto.dto.response.DevolucaoResponseDTO;
import br.com.estoquecerto.mapper.DevolucaoMapper;
import br.com.estoquecerto.service.DevolucaoService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class DevolucaoController {

    private final DevolucaoService devolucaoService;

    public DevolucaoController() {
        this.devolucaoService = new DevolucaoService();
    }

    // Cadastrar devolução
    public Long cadastrar(DevolucaoRequestDTO dto) throws SQLException {

        if (dto == null) {
            throw new IllegalArgumentException(
                "Dados da devolução não informados."
            );
        }

        List<DevolucaoService.NovoItem> itens = dto.getItens() == null
            ? List.of()
            : dto.getItens().stream()
                .map(item -> item == null
                    ? null
                    : new DevolucaoService.NovoItem(
                        item.getItemCompraId(),
                        item.getQuantidade()))
                .toList();

        return devolucaoService.cadastrar(
            dto.getCompraId(),
            converterData(dto.getDataDevolucao()),
            dto.getMotivo(),
            dto.getDescricao(),
            itens
        );
    }

    // Buscar devolução pelo ID
    public Optional<DevolucaoResponseDTO> buscarPorId(Long id)
            throws SQLException {

        return devolucaoService.buscarPorId(id)
                .map(DevolucaoMapper::paraResponseDTO);
    }

    // Listar devoluções
    public List<DevolucaoResponseDTO> listarTodos() throws SQLException {

        return devolucaoService.listarTodos()
                .stream()
                .map(DevolucaoMapper::paraResponseDTO)
                .toList();
    }

    // Listar devoluções de uma compra
    public List<DevolucaoResponseDTO> listarPorCompra(Long compraId)
            throws SQLException {

        return devolucaoService.listarPorCompra(compraId)
                .stream()
                .map(DevolucaoMapper::paraResponseDTO)
                .toList();
    }

    // Concluir devolução
    public int concluir(Long id) throws SQLException {

        return devolucaoService.concluir(id);
    }

    // Cancelar devolução
    public void cancelar(Long id) throws SQLException {

        devolucaoService.cancelar(id);
    }

    // Data opcional no formato AAAA-MM-DD
    private LocalDate converterData(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(texto.strip());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Data da devolução inválida. Use o formato AAAA-MM-DD."
            );
        }
    }
}
