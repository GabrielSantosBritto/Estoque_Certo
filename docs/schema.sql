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

-- ---------------------------------------------------------------------
-- Movimentações de estoque (histórico: só INSERT e SELECT)
--
-- item_compra_id e item_devolucao_id ainda não têm chave estrangeira
-- porque as tabelas itens_compra e itens_devolucao pertencem aos módulos
-- de Compra e Devolução. Quem criar essas tabelas deve acrescentar, logo
-- abaixo delas:
--
--   ALTER TABLE movimentacoes_estoque
--       ADD CONSTRAINT fk_movimentacoes_item_compra
--       FOREIGN KEY (item_compra_id) REFERENCES itens_compra (id);
--
--   ALTER TABLE movimentacoes_estoque
--       ADD CONSTRAINT fk_movimentacoes_item_devolucao
--       FOREIGN KEY (item_devolucao_id) REFERENCES itens_devolucao (id);
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movimentacoes_estoque (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    produto_id         BIGINT         NOT NULL,
    tipo               VARCHAR(10)    NOT NULL,
    origem             VARCHAR(15)    NOT NULL,
    quantidade         DECIMAL(12,3)  NOT NULL,
    data_movimentacao  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    item_compra_id     BIGINT         NULL,
    item_devolucao_id  BIGINT         NULL,
    motivo             VARCHAR(255)   NULL,

    CONSTRAINT pk_movimentacoes_estoque PRIMARY KEY (id),
    CONSTRAINT fk_movimentacoes_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id),

    CONSTRAINT ck_movimentacoes_tipo
        CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    CONSTRAINT ck_movimentacoes_origem
        CHECK (origem IN ('COMPRA', 'VENDA', 'DEVOLUCAO', 'AJUSTE')),
    CONSTRAINT ck_movimentacoes_quantidade
        CHECK (quantidade > 0),

    -- Cada origem exige o seu vínculo
    CONSTRAINT ck_movimentacoes_vinculo_compra
        CHECK (origem <> 'COMPRA' OR item_compra_id IS NOT NULL),
    CONSTRAINT ck_movimentacoes_vinculo_devolucao
        CHECK (origem <> 'DEVOLUCAO' OR item_devolucao_id IS NOT NULL),
    CONSTRAINT ck_movimentacoes_motivo_ajuste
        CHECK (origem <> 'AJUSTE' OR motivo IS NOT NULL),

    INDEX idx_movimentacoes_produto_data (produto_id, data_movimentacao)
) ENGINE = InnoDB;
