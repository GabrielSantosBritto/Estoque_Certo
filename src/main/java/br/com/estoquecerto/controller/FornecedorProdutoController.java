package br.com.estoquecerto.controller;

import br.com.estoquecerto.dto.request.FornecedorProdutoRequestDTO;
import br.com.estoquecerto.dto.response.FornecedorProdutoResponseDTO;
import br.com.estoquecerto.mapper.FornecedorProdutoMapper;
import br.com.estoquecerto.service.FornecedorProdutoService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FornecedorProdutoController {

    private final FornecedorProdutoService fornecedorProdutoService;

    public FornecedorProdutoController() {
        this.fornecedorProdutoService = new FornecedorProdutoService();
    }

    // Associar produto a fornecedor
    public Long associar(FornecedorProdutoRequestDTO dto)
            throws SQLException {

        if (dto == null) {
            throw new IllegalArgumentException(
                "Dados da associação não informados."
            );
        }

        return fornecedorProdutoService.associar(
            dto.getFornecedorId(),
            dto.getProdutoId()
        );
    }

    // Buscar associação pelo ID
    public Optional<FornecedorProdutoResponseDTO> buscarPorId(Long id)
            throws SQLException {

        return fornecedorProdutoService.buscarPorId(id)
                .map(FornecedorProdutoMapper::paraResponseDTO);
    }

    // Listar todas as associações
    public List<FornecedorProdutoResponseDTO> listarTodos()
            throws SQLException {

        return fornecedorProdutoService.listarTodos()
                .stream()
                .map(FornecedorProdutoMapper::paraResponseDTO)
                .toList();
    }

    // Listar produtos de um fornecedor
    public List<FornecedorProdutoResponseDTO> listarPorFornecedor(
            Long fornecedorId) throws SQLException {

        return fornecedorProdutoService.listarPorFornecedor(fornecedorId)
                .stream()
                .map(FornecedorProdutoMapper::paraResponseDTO)
                .toList();
    }

    // Excluir associação
    public void excluir(Long id) throws SQLException {

        fornecedorProdutoService.excluir(id);
    }
}
