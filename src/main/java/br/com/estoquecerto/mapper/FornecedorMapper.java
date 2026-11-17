package br.com.estoquecerto.mapper;

import br.com.estoquecerto.dto.request.FornecedorRequestDTO;
import br.com.estoquecerto.dto.response.FornecedorResponseDTO;
import br.com.estoquecerto.model.entity.Fornecedor;

public class FornecedorMapper {

    // Converter RequestDTO para entidade
    public static Fornecedor paraEntidade(FornecedorRequestDTO dto) {

        return new Fornecedor(
            dto.getCnpj(),
            dto.getRazaoSocial(),
            dto.getNomeFantasia(),
            dto.getInscricaoEstadual(),
            dto.getTelefone(),
            dto.getEmail(),
            dto.getCep(),
            dto.getLogradouro(),
            dto.getNumero(),
            dto.getComplemento(),
            dto.getBairro(),
            dto.getMunicipio(),
            dto.getUf(),
            dto.getObservacoes()
        );
    }

    // Converter entidade para ResponseDTO
    public static FornecedorResponseDTO paraResponseDTO(Fornecedor fornecedor) {

        return new FornecedorResponseDTO(
            fornecedor.getId(),
            fornecedor.getCnpj(),
            fornecedor.getRazaoSocial(),
            fornecedor.getNomeFantasia(),
            fornecedor.getInscricaoEstadual(),
            fornecedor.getTelefone(),
            fornecedor.getEmail(),
            fornecedor.getCep(),
            fornecedor.getLogradouro(),
            fornecedor.getNumero(),
            fornecedor.getComplemento(),
            fornecedor.getBairro(),
            fornecedor.getMunicipio(),
            fornecedor.getUf(),
            fornecedor.getObservacoes(),
            fornecedor.isAtivo()
        );
    }
}

