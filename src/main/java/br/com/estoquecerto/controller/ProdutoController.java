package br.com.estoquecerto.controller;

import br.com.estoquecerto.dto.request.ProdutoRequestDTO;
import br.com.estoquecerto.dto.response.ProdutoResponseDTO;
import br.com.estoquecerto.mapper.ProdutoMapper;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.service.ProdutoService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController() {
        this.produtoService = new ProdutoService();
    }

    public Long cadastrar(ProdutoRequestDTO dto)
            throws SQLException {

        validarDto(dto);

        Produto produto = ProdutoMapper.paraEntidade(dto);

        return produtoService.cadastrar(produto);
    }

    public Optional<ProdutoResponseDTO> buscarPorId(Long id)
            throws SQLException {

        return produtoService.buscarPorId(id)
                .map(ProdutoMapper::paraResponseDTO);
    }

    public Optional<ProdutoResponseDTO> buscarPorCodigo(String codigo)
            throws SQLException {

        return produtoService.buscarPorCodigo(codigo)
                .map(ProdutoMapper::paraResponseDTO);
    }

    public List<ProdutoResponseDTO> listarTodos()
            throws SQLException {

        return produtoService.listarTodos()
                .stream()
                .map(ProdutoMapper::paraResponseDTO)
                .toList();
    }

    // Atualizar produto
    public void atualizar(Long id, ProdutoRequestDTO dto)
            throws SQLException {

        validarDto(dto);

        Produto produto = ProdutoMapper.paraEntidade(dto);

        produtoService.atualizar(id, produto);
    }

    // Desativar produto
    public void desativar(Long id) throws SQLException {

        produtoService.desativar(id);
    }

    private void validarDto(ProdutoRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                "Dados do produto não informados."
            );
        }
    }
}
