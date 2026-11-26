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
-- Compras
-- O total não é armazenado: é calculado a partir dos itens, do frete
-- e do desconto.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS compras (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    fornecedor_id   BIGINT         NOT NULL,
    data_compra     DATE           NOT NULL,
    frete           DECIMAL(12,2)  NOT NULL DEFAULT 0,
    desconto        DECIMAL(12,2)  NOT NULL DEFAULT 0,
    status          VARCHAR(10)    NOT NULL DEFAULT 'PENDENTE',

    CONSTRAINT pk_compras PRIMARY KEY (id),
    CONSTRAINT fk_compras_fornecedor
        FOREIGN KEY (fornecedor_id) REFERENCES fornecedores (id),

    CONSTRAINT ck_compras_status
        CHECK (status IN ('PENDENTE', 'RECEBIDA', 'CANCELADA')),
    CONSTRAINT ck_compras_frete    CHECK (frete >= 0),
    CONSTRAINT ck_compras_desconto CHECK (desconto >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Itens da compra
-- preco_unitario é o preço de aquisição, diferente do preço de venda.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS itens_compra (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    compra_id       BIGINT         NOT NULL,
    produto_id      BIGINT         NOT NULL,
    quantidade      DECIMAL(12,3)  NOT NULL,
    preco_unitario  DECIMAL(12,2)  NOT NULL,

    CONSTRAINT pk_itens_compra PRIMARY KEY (id),
    CONSTRAINT fk_itens_compra_compra
        FOREIGN KEY (compra_id) REFERENCES compras (id),
    CONSTRAINT fk_itens_compra_produto
        FOREIGN KEY (produto_id) REFERENCES produtos (id),

    -- O mesmo produto não pode se repetir na mesma compra
    CONSTRAINT uk_itens_compra_produto UNIQUE (compra_id, produto_id),

    CONSTRAINT ck_itens_compra_quantidade     CHECK (quantidade > 0),
    CONSTRAINT ck_itens_compra_preco_unitario CHECK (preco_unitario > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Devoluções ao fornecedor
-- Só compras RECEBIDAS podem ter devolução. O estoque só é baixado
-- quando a devolução é CONCLUIDA.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS devolucoes (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    compra_id       BIGINT         NOT NULL,
    data_devolucao  DATE           NOT NULL,
    motivo          VARCHAR(30)    NOT NULL,
    descricao       VARCHAR(500)   NULL,
    status          VARCHAR(10)    NOT NULL DEFAULT 'PENDENTE',

    CONSTRAINT pk_devolucoes PRIMARY KEY (id),
    CONSTRAINT fk_devolucoes_compra
        FOREIGN KEY (compra_id) REFERENCES compras (id),

    CONSTRAINT ck_devolucoes_status
        CHECK (status IN ('PENDENTE', 'CONCLUIDA', 'CANCELADA')),
    CONSTRAINT ck_devolucoes_motivo
        CHECK (motivo IN ('PRODUTO_DANIFICADO', 'PRODUTO_COM_DEFEITO',
                          'PRODUTO_INCORRETO', 'QUANTIDADE_EXCEDENTE',
                          'PRODUTO_VENCIDO', 'OUTROS')),
    CONSTRAINT ck_devolucoes_descricao_outros
        CHECK (motivo <> 'OUTROS' OR descricao IS NOT NULL)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Itens da devolução
-- Cada linha aponta para o item da compra que está sendo devolvido.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS itens_devolucao (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    devolucao_id    BIGINT         NOT NULL,
    item_compra_id  BIGINT         NOT NULL,
    quantidade      DECIMAL(12,3)  NOT NULL,

    CONSTRAINT pk_itens_devolucao PRIMARY KEY (id),
    CONSTRAINT fk_itens_devolucao_devolucao
        FOREIGN KEY (devolucao_id) REFERENCES devolucoes (id),
    CONSTRAINT fk_itens_devolucao_item_compra
        FOREIGN KEY (item_compra_id) REFERENCES itens_compra (id),

    -- O mesmo item da compra não pode se repetir na mesma devolução
    CONSTRAINT uk_itens_devolucao_item UNIQUE (devolucao_id, item_compra_id),

    CONSTRAINT ck_itens_devolucao_quantidade CHECK (quantidade > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Movimentações de estoque (histórico: só INSERT e SELECT)
--
-- Esta tabela fica depois das tabelas de itens porque referencia
-- itens_compra e itens_devolucao.
--
-- Bancos criados antes dessas tabelas precisam, uma única vez, dos
-- ALTER TABLE da seção 10 do DOCUMENTACAO.md.
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
    CONSTRAINT fk_movimentacoes_item_compra
        FOREIGN KEY (item_compra_id) REFERENCES itens_compra (id),
    CONSTRAINT fk_movimentacoes_item_devolucao
        FOREIGN KEY (item_devolucao_id) REFERENCES itens_devolucao (id),

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
