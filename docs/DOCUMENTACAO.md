# EstoqueCerto --- Documentação Técnica

> Documento de referência do projeto. Atualizar este arquivo conforme as
> funcionalidades forem implementadas e testadas.

-   **Linguagem:** Java 26
-   **Build:** Maven
-   **Banco:** MySQL 8
-   **Persistência:** JDBC
-   **JSON:** Jackson
-   **Servidor HTTP:** `com.sun.net.httpserver.HttpServer`
-   **Testes de API:** Postman
-   **Versionamento:** Git e GitHub
-   **API externa prevista:** BrasilAPI (consulta de CNPJ)

## 1. Objetivo e escopo

O EstoqueCerto é um sistema de gestão de compras e controle de estoque
voltado a pequenos comércios. O sistema gerencia fornecedores, produtos,
compras, recebimentos, lotes, movimentações de estoque e devoluções.

A aplicação será uma API HTTP que recebe e devolve JSON. O Postman será
utilizado para testar as rotas. Não faz parte do escopo atual
desenvolver frontend, utilizar Spring Boot ou utilizar ORM como
Hibernate/JPA.

### Objetivos

-   Cadastrar e consultar fornecedores e produtos.
-   Relacionar fornecedores aos produtos que podem fornecer.
-   Registrar compras e seus itens.
-   Controlar o recebimento e a entrada de mercadorias no estoque.
-   Controlar lotes com estratégia FIFO.
-   Registrar movimentações de estoque.
-   Registrar devoluções ao fornecedor.
-   Disponibilizar operações por API HTTP.

## 2. Tecnologias

  Tecnologia       Uso
  ---------------- ------------------------------------
  Java 26          Linguagem principal
  Maven            Build e dependências
  MySQL 8          Banco relacional
  JDBC             Acesso SQL com `PreparedStatement`
  Jackson          Conversão JSON
  JDK HttpServer   Servidor HTTP
  Postman          Testes da API
  Git/GitHub       Versionamento e colaboração
  BrasilAPI        Consulta externa de CNPJ

A conexão com o banco utiliza as variáveis de ambiente `DB_URL`,
`DB_USER` e `DB_PASSWORD`. Credenciais não devem ser versionadas.

## 3. Arquitetura em camadas

Fluxo de uma requisição:

``` text
Cliente HTTP / Postman
        |
   ServidorHttp
        |
  FrontController
        |
    Controller
        |
     Service
        |
 Repository (JDBC)
        |
      MySQL
```

  Camada                      Responsabilidade
  --------------------------- ---------------------------------------------------
  `infrastructure/http`       Inicializar o servidor HTTP
  `controller`                Interpretar requisições e produzir respostas HTTP
  `dto/request`               Dados recebidos em JSON
  `dto/response`              Dados devolvidos em JSON
  `service`                   Regras e validações de negócio
  `repository`                Consultas e persistência JDBC
  `model/entity`              Entidades do domínio
  `model/enums`               Valores enumerados do domínio
  `mapper`                    Conversão entre DTOs e entidades
  `infrastructure/database`   Obtenção de conexões

Códigos HTTP esperados incluem `200 OK`, `201 Created`,
`400 Bad Request`, `404 Not Found` e `500 Internal Server Error`.
Respostas não devem expor credenciais ou detalhes internos do banco.

## 4. Entidades e regras de negócio

O domínio possui nove entidades principais.

### 4.1 Fornecedor

Representa uma empresa fornecedora.

**Atributos:** `id`, `cnpj`, `razaoSocial`, `nomeFantasia`,
`inscricaoEstadual`, `telefone`, `email`, `cep`, `logradouro`, `numero`,
`complemento`, `bairro`, `municipio`, `uf`, `observacoes`, `ativo`.

**Regras:** - CNPJ e razão social obrigatórios; CNPJ único. - Fornecedor
começa ativo. - Fornecedor inativo não pode ser usado em novas
compras. - Exclusão lógica, preservando histórico. - Atualização
cadastral não altera CNPJ nem estado de ativação.

**Estado:** Entity, DTOs, Mapper, Repository, Service e Controller
implementados. Cadastro, listagem, busca por ID, busca por CNPJ e
atualização foram testados no Postman. A desativação está implementada,
mas seu teste ainda está pendente.

### 4.2 Produto

Representa um item controlado pelo estoque.

**Atributos:** `id`, `codigo`, `nome`, `descricao`, `unidadeMedida`,
`quantidadeEstoque`, `quantidadeMinima`, `precoVenda`, `ativo`.

**Regras:** - Código SKU único, gerado no formato `PROD-0001`. - Estoque
inicial igual a zero. - Produto começa ativo. - Atualização cadastral
não altera diretamente o estoque. - Exclusão lógica por
`ativo = false`. - Entradas e saídas devem ocorrer pelos fluxos de
movimentação.

**Unidades previstas:** `UN`, `KG`, `L`, `M`, `CX`, `PCT`.

**Estado:** Entity, DTOs, Mapper, Repository, Service e Controller
existem. A tabela MySQL foi ajustada para usar `preco_venda` e `ativo`,
alinhando-a ao código Java. O CRUD ainda precisa ser validado
integralmente contra o banco.

### 4.3 FornecedorProduto

Entidade associativa entre fornecedor e produto.

**Atributos:** `id`, `fornecedor`, `produto`.

**Regras:** - Um fornecedor pode fornecer vários produtos. - Um produto
pode ter vários fornecedores. - O par fornecedor/produto não pode se
repetir. - O Service deve verificar se os dois registros existem e estão
ativos antes de associá-los.

**Estado:** Entity, RequestDTO, ResponseDTO, Mapper e Repository criados
e compilados. Tabela MySQL criada com chaves estrangeiras e restrição
única. Service, Controller, rotas e testes ainda pendentes.

### 4.4 Compra

Representa uma aquisição feita junto a um fornecedor.

**Atributos definidos:** `id`, `fornecedor`, `dataCompra` (`LocalDate`),
`frete` (`BigDecimal`), `desconto` (`BigDecimal`) e `status`
(`StatusCompra`).

**Regras:** - Status inicial `PENDENTE`. - Frete e desconto opcionais,
padrão 0. - Total calculado, não persistido como atributo. - Compra deve
possuir ao menos um item. - Só pode ser recebida ou cancelada enquanto
pendente. - Fornecedor deve estar ativo na criação. - Recebimento deve
atualizar estoque e gerar lotes em uma transação.

### 4.5 ItemCompra

Representa um produto adquirido dentro de uma compra.

**Atributos:** `id`, `produto`, `quantidade` (`BigDecimal`),
`precoUnitario` (`BigDecimal`).

**Regras:** - Quantidade e preço unitário maiores que zero. - Subtotal
calculado. - Preço unitário é o preço de aquisição, diferente do preço
de venda do Produto. - Mesmo produto não pode repetir na mesma compra. -
Itens só podem ser alterados enquanto a compra estiver pendente.

### 4.6 LoteEstoque

Representa o lote gerado a partir de um ItemCompra recebido.

**Atributos:** `id`, `itemCompra`, `quantidadeInicial`,
`quantidadeDisponivel` (`BigDecimal`), `dataEntrada` (`LocalDateTime`).

**Regras:** - Cada ItemCompra recebida gera um lote. - Quantidade
inicial é preservada. - Quantidade disponível não pode ser negativa. -
Lote permanece no histórico mesmo zerado. - Baixas seguem FIFO (First
In, First Out).

### 4.7 MovimentacaoEstoque

Registra entradas e saídas de estoque.

**Atributos:** `id`, `lote`, `itemDevolucao` (opcional), `tipo`,
`origem`, `quantidade`, `dataMovimentacao`, `motivo` (quando aplicável).

**Regras:** - Quantidade sempre positiva. - Tipos: `ENTRADA`, `SAIDA`. -
Origens: `COMPRA`, `VENDA`, `DEVOLUCAO`, `AJUSTE`. - Histórico não deve
ser editado ou apagado para corrigir erros; correções são novas
movimentações de ajuste. - Venda está prevista para evolução futura.

### 4.8 Devolucao

Representa uma devolução ao fornecedor vinculada a uma compra.

**Atributos:** `id`, `compra`, `dataDevolucao`, `motivo`
(`MotivoDevolucao`), `descricao` (opcional), `status`
(`StatusDevolucao`).

**Regras:** - Status inicial `PENDENTE`. - Deve possuir pelo menos um
item. - Só pode ser concluída ou cancelada enquanto pendente. -
Conclusão gera movimentações de saída. - Cancelamento não altera
estoque. - Conclusão deve ser transacional.

### 4.9 ItemDevolucao

Representa a quantidade de um lote incluída em uma devolução.

**Atributos:** `id`, `devolucao`, `lote`, `quantidade` (`BigDecimal`).

**Regras:** - Quantidade maior que zero e não superior à disponível no
lote. - Lote deve pertencer a um item da compra relacionada à
devolução. - Conclusão da devolução gera a movimentação de saída
correspondente.

## 5. Relacionamentos

  Origem        Destino               Cardinalidade
  ------------- --------------------- ---------------
  Fornecedor    FornecedorProduto     1:N
  Produto       FornecedorProduto     1:N
  Fornecedor    Compra                1:N
  Compra        ItemCompra            1:N
  Produto       ItemCompra            1:N
  ItemCompra    LoteEstoque           1:1
  LoteEstoque   MovimentacaoEstoque   1:N
  Compra        Devolucao             1:N
  Devolucao     ItemDevolucao         1:N
  LoteEstoque   ItemDevolucao         1:N

O relacionamento N:N entre Fornecedor e Produto é resolvido pela
entidade associativa `FornecedorProduto`.

Representação simplificada:

``` text
Fornecedor 1 --- N FornecedorProduto N --- 1 Produto

Fornecedor 1 --- N Compra
Compra 1 --- N ItemCompra
Produto 1 --- N ItemCompra
ItemCompra 1 --- 1 LoteEstoque
LoteEstoque 1 --- N MovimentacaoEstoque

Compra 1 --- N Devolucao
Devolucao 1 --- N ItemDevolucao
LoteEstoque 1 --- N ItemDevolucao
```

## 6. DTOs e Mappers

**RequestDTO:** representa os dados recebidos pelo endpoint. Exemplo de
associação:

``` json
{
  "fornecedorId": 2,
  "produtoId": 1
}
```

**ResponseDTO:** representa os dados devolvidos, sem expor toda a
entidade. Exemplo previsto:

``` json
{
  "id": 1,
  "fornecedorId": 2,
  "fornecedorRazaoSocial": "Fornecedor Teste LTDA",
  "produtoId": 1,
  "produtoNome": "Produto Exemplo",
  "produtoCodigo": "PROD-0001"
}
```

**Mapper:** converte DTOs e entidades. Validações e consultas ao banco
pertencem ao Service e Repository.

## 7. Padrões de projeto

### 7.1 Front Controller

**Categoria:** arquitetural.\
**Classe:** `controller/FrontController.java`.

Centraliza a entrada das requisições HTTP e direciona cada rota ao
Controller responsável. **Estado:** implementado.

### 7.2 DTO

**Categoria:** transferência de dados.\
**Pacotes:** `dto/request` e `dto/response`.

Separa os dados expostos pela API das entidades internas. **Estado:**
implementado nos módulos existentes.

### 7.3 DAO / Repository

**Categoria:** acesso a dados.\
**Pacote:** `repository`.

Centraliza SQL e persistência JDBC, utilizando `PreparedStatement` e
`try-with-resources`. Não será utilizado ORM. **Estado:** implementado
nos módulos existentes.

### 7.4 Command

**Categoria:** GoF comportamental.

Encapsula operações de negócio explícitas. Commands previstos: -
`ConfirmarRecebimentoCommand`: confirma recebimento de compra, gera
lotes e movimentações de entrada. - `ConcluirDevolucaoCommand`: conclui
devolução e gera movimentações de saída.

As operações devem preservar a consistência transacional. **Estado:**
planejado, ainda não implementado.

### 7.5 Factory Method

**Categoria:** GoF criacional.

Classe prevista: `MovimentacaoFactory`. Centralizará a criação de
movimentações de estoque conforme tipo e origem, evitando espalhar a
lógica de criação pelos Services e Commands. **Estado:** planejado,
ainda não implementado.

## 8. Integração externa --- BrasilAPI

A BrasilAPI será utilizada para consulta de dados cadastrais de CNPJ,
atendendo ao requisito funcional RF02.

### Fluxo previsto

1.  Cliente envia um CNPJ para consulta.
2.  Aplicação valida o formato básico.
3.  Um cliente HTTP próprio realiza a requisição à BrasilAPI.
4.  A aplicação interpreta o JSON retornado.
5.  Os dados são apresentados ao cliente para conferência.
6.  O usuário decide se deseja utilizar os dados no cadastro do
    fornecedor.

A consulta externa não deve cadastrar ou atualizar automaticamente o
fornecedor sem uma operação explícita do usuário.

### Organização sugerida

``` text
infrastructure/
└── external/
    └── BrasilApiClient.java
```

### Tratamento de falhas

-   CNPJ inválido ou não encontrado.
-   Timeout ou indisponibilidade da API.
-   Resposta JSON inesperada.
-   Erros HTTP externos.

A indisponibilidade da BrasilAPI não deve expor detalhes internos nem
impedir necessariamente o cadastro manual conforme as regras locais.

**Estado:** integração planejada. Cliente HTTP, endpoint exato,
mapeamento de resposta e teste isolado ainda precisam ser implementados
e confirmados na documentação oficial da BrasilAPI. Não assumir campos
ou contrato externo antes dessa verificação.

## 9. API HTTP

Rotas existentes e previstas. Rotas planejadas não devem ser
consideradas implementadas até estarem no FrontController e testadas.

### 9.1 Status

  Método   Rota            Descrição                         Estado
  -------- --------------- --------------------------------- --------------
  GET      `/api/status`   Verifica disponibilidade da API   Implementada

### 9.2 Produtos

  ---------------------------------------------------------------------------------------
  Método            Rota                              Descrição         Estado
  ----------------- --------------------------------- ----------------- -----------------
  POST              `/api/produtos`                   Cadastra produto  Código existente

  GET               `/api/produtos`                   Lista produtos    Código existente

  GET               `/api/produtos/codigo/{codigo}`   Busca por código  Código existente

  GET               `/api/produtos/{id}`              Busca por ID      Código existente

  PUT               `/api/produtos/{id}`              Atualiza produto  Código existente

  DELETE            `/api/produtos/{id}`              Desativa produto  Código existente
  ---------------------------------------------------------------------------------------

### 9.3 Fornecedores

  ---------------------------------------------------------------------------------------
  Método            Rota                              Descrição         Estado
  ----------------- --------------------------------- ----------------- -----------------
  POST              `/api/fornecedores`               Cadastra          Implementada e
                                                      fornecedor        testada

  GET               `/api/fornecedores`               Lista             Implementada e
                                                      fornecedores      testada

  GET               `/api/fornecedores/cnpj/{cnpj}`   Busca por CNPJ    Implementada e
                                                                        testada

  GET               `/api/fornecedores/{id}`          Busca por ID      Implementada e
                                                                        testada

  PUT               `/api/fornecedores/{id}`          Atualiza          Implementada e
                                                      fornecedor        testada

  DELETE            `/api/fornecedores/{id}`          Desativa          Código existente;
                                                      fornecedor        teste pendente
  ---------------------------------------------------------------------------------------

A rota por CNPJ deve ser verificada antes da rota genérica por ID.

### 9.4 FornecedorProduto --- planejadas

  ---------------------------------------------------------------------------------------------
  Método                  Rota                                          Descrição
  ----------------------- --------------------------------------------- -----------------------
  POST                    `/api/fornecedores-produtos`                  Cria associação

  GET                     `/api/fornecedores-produtos`                  Lista associações

  GET                     `/api/fornecedores-produtos/{id}`             Busca associação por ID

  GET                     `/api/fornecedores/{fornecedorId}/produtos`   Lista produtos do
                                                                        fornecedor

  DELETE                  `/api/fornecedores-produtos/{id}`             Exclui associação
  ---------------------------------------------------------------------------------------------

### 9.5 Compras e devoluções

As rotas completas serão definidas durante a implementação dos
respectivos Controllers. Deverão contemplar cadastro, consulta,
operações permitidas de atualização, gerenciamento de itens,
recebimento/cancelamento de compra e conclusão/cancelamento de
devolução.

## 10. Banco de dados

Tabelas verificadas no estado atual:

-   `fornecedores`
-   `produtos`
-   `fornecedor_produto`

A tabela `fornecedor_produto` possui: - `id`: PK auto incremento. -
`fornecedor_id`: FK para `fornecedores(id)`. - `produto_id`: FK para
`produtos(id)`. - `UNIQUE (fornecedor_id, produto_id)`.

A tabela `produtos` foi alinhada ao código Java com `preco_venda` e
`ativo`. No momento da conferência, ela estava vazia.

### Regras de persistência

-   Usar `PreparedStatement`.
-   Fechar recursos com `try-with-resources`.
-   Não concatenar valores de usuário diretamente em SQL.
-   Utilizar transações em operações com várias gravações relacionadas.
-   Preservar o histórico de compras, lotes e movimentações.
-   Não usar exclusão em cascata sem decisão explícita de domínio.

## 11. Estrutura de diretórios

Estrutura de referência; arquivos futuros serão adicionados conforme a
implementação:

``` text
src/main/java/br/com/estoquecerto/
├── EstoqueCertoApplication.java
├── controller/
│   ├── FrontController.java
│   ├── StatusController.java
│   ├── ProdutoController.java
│   ├── FornecedorController.java
│   └── FornecedorProdutoController.java       (previsto)
├── dto/
│   ├── request/
│   └── response/
├── infrastructure/
│   ├── database/
│   │   └── DatabaseConnection.java
│   ├── http/
│   │   └── ServidorHttp.java
│   └── external/
│       └── BrasilApiClient.java               (previsto)
├── mapper/
├── model/
│   ├── entity/
│   └── enums/
├── repository/
└── service/
```

## 12. Testes e critérios de conclusão

### Compilação

``` powershell
mvn clean compile
```

Resultado esperado: `BUILD SUCCESS`.

### Testes de API

No Postman, verificar método, URL, JSON, código HTTP, corpo da resposta
e persistência real no MySQL. Também testar dados inválidos e recursos
inexistentes.

### BrasilAPI

Criar teste isolado demonstrando envio do CNPJ, recebimento da resposta,
interpretação dos dados e tratamento de falhas.

### Transações

Recebimento de compra e conclusão de devolução devem confirmar todas as
alterações em conjunto ou revertê-las em caso de falha.

## 13. Git e colaboração

Fluxo de trabalho acordado:

1.  Atualizar a `master`.
2.  Criar branch `feature/...`.
3.  Implementar uma funcionalidade isolada.
4.  Compilar e testar.
5.  Criar commits descritivos.
6.  Enviar a branch ao GitHub.
7.  Abrir Pull Request para `master`.
8.  Solicitar revisão antes do merge.

Não desenvolver diretamente na `master` nem realizar merge sem revisão
combinada pela equipe.

## 14. Estado atual e próximos passos

### Concluído ou existente

-   Estrutura Maven e servidor HTTP.
-   Front Controller.
-   Conexão JDBC com MySQL.
-   Módulo de fornecedores com operações principais testadas.
-   Código do módulo de produtos.
-   Ajuste da tabela `produtos` para `preco_venda` e `ativo`.
-   Entity, DTOs, Mapper e Repository de `FornecedorProduto`.
-   Tabela `fornecedor_produto` com FKs e restrição de unicidade.

### Próximas atividades

1.  Implementar `FornecedorProdutoService`.
2.  Implementar `FornecedorProdutoController`.
3.  Adicionar rotas ao `FrontController`.
4.  Testar associações no Postman.
5.  Validar CRUD de Produto com MySQL.
6.  Implementar Compra e ItemCompra.
7.  Implementar LoteEstoque e MovimentacaoEstoque.
8.  Implementar Devolucao e ItemDevolucao.
9.  Aplicar Command e Factory Method.
10. Implementar e testar integração BrasilAPI.
11. Completar coleção Postman e documentação final.

------------------------------------------------------------------------

## 15. Manutenção deste documento

Este arquivo deve acompanhar a evolução real do sistema. Quando uma
funcionalidade for implementada, compilada e testada, atualizar seu
estado. Rotas, atributos, regras e contratos externos só devem ser
registrados como definitivos após confirmação no código, banco ou
documentação oficial correspondente.

O objetivo é manter uma referência comum para toda a equipe e reduzir
divergências entre modelagem, código, banco e API.
