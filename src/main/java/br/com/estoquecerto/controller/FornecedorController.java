
package br.com.estoquecerto.controller;

import br.com.estoquecerto.dto.request.FornecedorRequestDTO;
import br.com.estoquecerto.dto.response.FornecedorResponseDTO;
import br.com.estoquecerto.mapper.FornecedorMapper;
import br.com.estoquecerto.model.entity.Fornecedor;
import br.com.estoquecerto.service.FornecedorService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController() {
        this.fornecedorService = new FornecedorService();
    }

    // Cadastrar fornecedor
    public Long cadastrar(FornecedorRequestDTO dto)
            throws SQLException {

        if (dto == null) {
            throw new IllegalArgumentException(
                "Dados do fornecedor não informados."
            );
        }

        Fornecedor fornecedor = FornecedorMapper.paraEntidade(dto);

        return fornecedorService.cadastrar(fornecedor);
    }

    // Buscar fornecedor pelo ID
    public Optional<FornecedorResponseDTO> buscarPorId(Long id)
            throws SQLException {

        return fornecedorService.buscarPorId(id)
                .map(FornecedorMapper::paraResponseDTO);
    }

    // Buscar fornecedor pelo CNPJ
    public Optional<FornecedorResponseDTO> buscarPorCnpj(String cnpj)
            throws SQLException {

        return fornecedorService.buscarPorCnpj(cnpj)
                .map(FornecedorMapper::paraResponseDTO);
    }

    // Listar todos os fornecedores
    public List<FornecedorResponseDTO> listarTodos()
            throws SQLException {

        return fornecedorService.listarTodos()
                .stream()
                .map(FornecedorMapper::paraResponseDTO)
                .toList();
    }

    // Atualizar fornecedor
    public void atualizar(Long id, FornecedorRequestDTO dto)
            throws SQLException {

        if (dto == null) {
            throw new IllegalArgumentException(
                "Dados do fornecedor não informados."
            );
        }

        Fornecedor fornecedor = FornecedorMapper.paraEntidade(dto);

        fornecedorService.atualizar(id, fornecedor);
    }

    // Desativar fornecedor
    public void desativar(Long id) throws SQLException {

        fornecedorService.desativar(id);
    }
}

