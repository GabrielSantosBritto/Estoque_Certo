-- =====================================================================
-- EstoqueCerto - Script de criação do banco de dados (MySQL 8)
--
-- Como executar (terminal):
--   mysql -u root -p < docs/schema.sql
--
-- O script é seguro para rodar mais de uma vez: não apaga tabelas
-- nem dados existentes.
--
-- Ao implementar um novo módulo (compras, movimentações, devoluções),
-- acrescente a tabela correspondente ao final deste arquivo.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS estoquecerto
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE estoquecerto;

-- ---------------------------------------------------------------------
-- Fornecedores
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fornecedores (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    cnpj                VARCHAR(18)   NOT NULL,
    razao_social        VARCHAR(150)  NOT NULL,
    nome_fantasia       VARCHAR(150)  NULL,
    inscricao_estadual  VARCHAR(20)   NULL,
    telefone            VARCHAR(20)   NULL,
    email               VARCHAR(150)  NULL,
    cep                 VARCHAR(9)    NULL,
    logradouro          VARCHAR(150)  NULL,
    numero              VARCHAR(20)   NULL,
    complemento         VARCHAR(100)  NULL,
    bairro              VARCHAR(100)  NULL,
    municipio           VARCHAR(100)  NULL,
    uf                  VARCHAR(2)    NULL,
    observacoes         VARCHAR(500)  NULL,
    ativo               BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_fornecedores       PRIMARY KEY (id),
    CONSTRAINT uk_fornecedores_cnpj  UNIQUE (cnpj)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Produtos
-- O código (SKU) é gerado pela aplicação no formato PROD-0001.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produtos (
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    codigo              VARCHAR(50)    NOT NULL,
    nome                VARCHAR(120)   NOT NULL,
    descricao           VARCHAR(500)   NULL,
    unidade_medida      VARCHAR(10)    NULL,
    quantidade_estoque  DECIMAL(12,3)  NOT NULL DEFAULT 0,
    quantidade_minima   DECIMAL(12,3)  NOT NULL DEFAULT 0,
    preco_venda         DECIMAL(12,2)  NULL,
    ativo               BOOLEAN        NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_produtos         PRIMARY KEY (id),
    CONSTRAINT uk_produtos_codigo  UNIQUE (codigo)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Associação Fornecedor x Produto (N:N)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fornecedor_produto (
    id             BIGINT  NOT NULL AUTO_INCREMENT,
    fornecedor_id  BIGINT  NOT NULL,
    produto_id     BIGINT  NOT NULL,

    CONSTRAINT pk_fornecedor_produto PRIMARY KEY (id),
    CONSTRAINT uk_fornecedor_produto UNIQUE (fornecedor_id, produto_id),
    CONSTRAINT fk_fornecedor_produto_fornecedor
        FOREIGN KEY (fornecedor_id) REFERENCES fornecedores (id),
    CONSTRAINT fk_fornecedor_produto_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id)
) ENGINE = InnoDB;
