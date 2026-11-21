package br.com.estoquecerto.controller;

import br.com.estoquecerto.dto.request.AjusteEstoqueRequestDTO;
import br.com.estoquecerto.dto.response.MovimentacaoResponseDTO;
import br.com.estoquecerto.mapper.MovimentacaoMapper;
import br.com.estoquecerto.service.MovimentacaoService;

import java.sql.SQLException;
import java.util.List;

public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController() {
        this.movimentacaoService = new MovimentacaoService();
    }

    // Registrar ajuste manual de estoque
    public Long registrarAjuste(AjusteEstoqueRequestDTO dto)
            throws SQLException {

        if (dto == null) {
            throw new IllegalArgumentException(
                "Dados do ajuste não informados."
            );
        }

        return movimentacaoService.registrarAjuste(
            dto.getProdutoId(),
            dto.getTipo(),
            dto.getQuantidade(),
            dto.getMotivo()
        );
    }

    // Listar movimentações de um produto
    public List<MovimentacaoResponseDTO> listarPorProduto(Long produtoId)
            throws SQLException {

        return movimentacaoService.listarPorProduto(produtoId)
                .stream()
                .map(MovimentacaoMapper::paraResponseDTO)
                .toList();
    }
}
