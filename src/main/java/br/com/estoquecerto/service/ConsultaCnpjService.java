package br.com.estoquecerto.service;

import br.com.estoquecerto.dto.response.ConsultaCnpjResponseDTO;
import br.com.estoquecerto.infrastructure.external.BrasilApiClient;
import com.fasterxml.jackson.databind.JsonNode;

public class ConsultaCnpjService {

    private final BrasilApiClient brasilApiClient;

    public ConsultaCnpjService() {
        this(new BrasilApiClient());
    }

    public ConsultaCnpjService(BrasilApiClient brasilApiClient) {
        this.brasilApiClient = brasilApiClient;
    }

    public ConsultaCnpjResponseDTO consultar(String cnpjInformado) {
        String cnpjLimpo = limparEValidarCnpj(cnpjInformado);
        JsonNode json = brasilApiClient.buscarCnpj(cnpjLimpo);

        String cnpjRetornado = primeiroTextoPreenchido(json, "cnpj");
        if (cnpjRetornado == null) {
            cnpjRetornado = cnpjLimpo;
        }

        return new ConsultaCnpjResponseDTO(
                cnpjRetornado,
                textoOuNulo(json, "razao_social"),
                textoOuNulo(json, "nome_fantasia"),
                primeiroTextoPreenchido(json, "ddd_telefone_1", "ddd_telefone_2"),
                textoOuNulo(json, "email"),
                textoOuNulo(json, "cep"),
                textoOuNulo(json, "logradouro"),
                textoOuNulo(json, "numero"),
                textoOuNulo(json, "complemento"),
                textoOuNulo(json, "bairro"),
                textoOuNulo(json, "municipio"),
                textoOuNulo(json, "uf"),
                textoOuNulo(json, "descricao_situacao_cadastral")
        );
    }

    private String limparEValidarCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ é obrigatório.");
        }
        String apenasDigitos = cnpj.replaceAll("\\D", "");
        if (apenasDigitos.length() != 14) {
            throw new IllegalArgumentException("CNPJ inválido. Deve conter exatamente 14 dígitos.");
        }
        return apenasDigitos;
    }

    private String textoOuNulo(JsonNode json, String campo) {
        if (json == null || !json.has(campo) || json.get(campo).isNull()) {
            return null;
        }
        String valor = json.get(campo).asText().trim();
        return valor.isEmpty() ? null : valor;
    }

    private String primeiroTextoPreenchido(JsonNode json, String... campos) {
        for (String campo : campos) {
            String valor = textoOuNulo(json, campo);
            if (valor != null) {
                return valor;
            }
        }
        return null;
    }
}