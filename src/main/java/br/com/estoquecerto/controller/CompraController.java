package br.com.estoquecerto.controller;

import br.com.estoquecerto.dto.request.CompraRequestDTO;
import br.com.estoquecerto.dto.response.CompraResponseDTO;
import br.com.estoquecerto.mapper.CompraMapper;
import br.com.estoquecerto.service.CompraService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class CompraController {

    private final CompraService compraService;

    public CompraController() {
        this.compraService = new CompraService();
    }

    // Cadastrar compra
    public Long cadastrar(CompraRequestDTO dto) throws SQLException {

        if (dto == null) {
            throw new IllegalArgumentException(
                "Dados da compra não informados."
            );
        }

        List<CompraService.NovoItem> itens = dto.getItens() == null
            ? List.of()
            : dto.getItens().stream()
                .map(item -> item == null
                    ? null
                    : new CompraService.NovoItem(
                        item.getProdutoId(),
                        item.getQuantidade(),
                        item.getPrecoUnitario()))
                .toList();

        return compraService.cadastrar(
            dto.getFornecedorId(),
            converterData(dto.getDataCompra()),
            dto.getFrete(),
            dto.getDesconto(),
            itens
        );
    }

    // Buscar compra pelo ID
    public Optional<CompraResponseDTO> buscarPorId(Long id)
            throws SQLException {

        return compraService.buscarPorId(id)
                .map(CompraMapper::paraResponseDTO);
    }

    // Listar compras
    public List<CompraResponseDTO> listarTodos() throws SQLException {

        return compraService.listarTodos()
                .stream()
                .map(CompraMapper::paraResponseDTO)
                .toList();
    }

    // Cancelar compra
    public void cancelar(Long id) throws SQLException {

        compraService.cancelar(id);
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
                "Data da compra inválida. Use o formato AAAA-MM-DD."
            );
        }
    }
}
