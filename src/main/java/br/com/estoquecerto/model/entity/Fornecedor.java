package br.com.estoquecerto.model.entity;

public class Fornecedor {

    private Long id;
    private String cnpj;
    private String razaoSocial;
    private String nomeFantasia;
    private String inscricaoEstadual;
    private String telefone;
    private String email;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String municipio;
    private String uf;
    private String observacoes;
    private boolean ativo;

    // Construtor completo: utilizado nas consultas ao banco
    public Fornecedor(
            Long id,
            String cnpj,
            String razaoSocial,
            String nomeFantasia,
            String inscricaoEstadual,
            String telefone,
            String email,
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String municipio,
            String uf,
            String observacoes,
            boolean ativo) {

        this.id = id;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
        this.telefone = telefone;
        this.email = email;
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.municipio = municipio;
        this.uf = uf;
        this.observacoes = observacoes;
        this.ativo = ativo;
    }

    // Construtor para cadastro
    public Fornecedor(
            String cnpj,
            String razaoSocial,
            String nomeFantasia,
            String inscricaoEstadual,
            String telefone,
            String email,
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String municipio,
            String uf,
            String observacoes) {

        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
        this.telefone = telefone;
        this.email = email;
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.municipio = municipio;
        this.uf = uf;
        this.observacoes = observacoes;
        this.ativo = true;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getMunicipio() {
        return municipio;
    }

    public String getUf() {
        return uf;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public boolean isAtivo() {
        return ativo;
    }

    // Atualizar dados cadastrais
    public void atualizarDados(
            String razaoSocial,
            String nomeFantasia,
            String inscricaoEstadual,
            String telefone,
            String email,
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String municipio,
            String uf,
            String observacoes) {

        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
        this.telefone = telefone;
        this.email = email;
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.municipio = municipio;
        this.uf = uf;
        this.observacoes = observacoes;
    }

    // Desativação lógica
    public void desativar() {
        this.ativo = false;
    }
}
