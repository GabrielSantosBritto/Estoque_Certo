package br.com.estoquecerto.controller;

import br.com.estoquecerto.dto.response.ConsultaCnpjResponseDTO;
import br.com.estoquecerto.service.ConsultaCnpjService;

public class ConsultaCnpjController {

    private final ConsultaCnpjService consultaCnpjService;

    public ConsultaCnpjController() {
        this.consultaCnpjService = new ConsultaCnpjService();
    }

    public ConsultaCnpjResponseDTO consultar(String cnpj) {
        return consultaCnpjService.consultar(cnpj);
    }
}