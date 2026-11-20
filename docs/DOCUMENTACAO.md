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
compras, recebimentos, movimentações de estoque e devoluções.

A aplicação será uma API HTTP que recebe e devolve JSON. O Postman será
utilizado para testar as rotas. Não faz parte do escopo atual
desenvolver frontend, utilizar Spring Boot ou utilizar ORM como
Hibernate/JPA.

### Objetivos

-   Cadastrar e consultar fornecedores e produtos.
-   Relacionar fornecedores aos produtos que podem fornecer.
-   Registrar compras e seus itens.
-   Controlar o recebimento e a entrada de mercadorias no estoque.
-   Registrar movimentações de estoque.
-   Registrar devoluções ao fornecedor.
-   Disponibilizar operações por API HTTP.

### Decisão de escopo (17/11)

Por causa do prazo de entrega (27/11), o controle de **lotes com
estratégia FIFO foi retirado do escopo**. O estoque passa a ser
controlado por produto, e todo o histórico continua registrado em
`MovimentacaoEstoque`. O controle por lote fica registrado como evolução
futura (seção 15) e não deve ser implementado sem nova decisão da equipe.

A integração com a **BrasilAPI permanece obrigatória**, pois atende ao
requisito de consumo de API externa.

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

### Tratamento de erros e códigos HTTP

O `FrontController` centraliza o tratamento de exceções. Os Services
**não conhecem HTTP**: lançam a exceção que descreve o problema, e o
FrontController a traduz no código de status.

| Situação | Exceção lançada | Status |
|---|---|---|
| Operação bem-sucedida | --- | `200 OK` |
| Recurso criado | --- | `201 Created` |
| JSON vazio, malformado ou com campo desconhecido | `JsonProcessingException` | `400 Bad Request` |
| Dado inválido ou regra de negócio violada | `IllegalArgumentException` | `400 Bad Request` |
| Recurso inexistente | `RecursoNaoEncontradoException` | `404 Not Found` |
| Duplicidade ou estado incompatível | `ConflitoException` | `409 Conflict` |
| Falha no banco ou erro inesperado | `SQLException` / outras | `500 Internal Server Error` |

As exceções próprias ficam no pacote `exception`. Ao implementar um
módulo novo, usar sempre essas exceções em vez de criar respostas HTTP
dentro do Service.

Respostas não devem expor credenciais ou detalhes internos do banco.

## 4. Entidades e regras de negócio

O domínio possui sete entidades principais.

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
- `quantidadeEstoque` só é alterada junto com o registro de uma
`MovimentacaoEstoque`, **na mesma transação**. Nenhuma rota altera o
estoque diretamente.

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

**Estado:** Entity, DTOs, Mapper, Repository, Service, Controller e
rotas implementados e compilados (branch `feature/fornecedor-produto`).
Tabela MySQL criada com chaves estrangeiras e restrição única.
Testes no Postman pendentes.

### 4.4 Compra

Representa uma aquisição feita junto a um fornecedor.

**Atributos definidos:** `id`, `fornecedor`, `dataCompra` (`LocalDate`),
`frete` (`BigDecimal`), `desconto` (`BigDecimal`) e `status`
(`StatusCompra`).

**Regras:** - Status inicial `PENDENTE`. - Frete e desconto opcionais,
padrão 0. - Total calculado, não persistido como atributo. - Compra deve
possuir ao menos um item. - Só pode ser recebida ou cancelada enquanto
pendente. - Fornecedor deve estar ativo na criação. - O recebimento gera
uma movimentação de ENTRADA por item e atualiza o estoque de cada
produto, tudo em uma única transação.

### 4.5 ItemCompra

Representa um produto adquirido dentro de uma compra.

**Atributos:** `id`, `produto`, `quantidade` (`BigDecimal`),
`precoUnitario` (`BigDecimal`).

**Regras:** - Quantidade e preço unitário maiores que zero. - Subtotal
calculado. - Preço unitário é o preço de aquisição, diferente do preço
de venda do Produto. - Mesmo produto não pode repetir na mesma compra. -
Itens só podem ser alterados enquanto a compra estiver pendente.

### 4.6 MovimentacaoEstoque

Registra entradas e saídas de estoque.

**Atributos:** `id`, `produto`, `tipo`, `origem`, `quantidade`,
`dataMovimentacao`, `itemCompra` (quando a origem é COMPRA),
`itemDevolucao` (quando a origem é DEVOLUCAO), `motivo` (obrigatório
quando a origem é AJUSTE).

**Regras:** - Quantidade sempre positiva. - Tipos: `ENTRADA`, `SAIDA`. -
Origens: `COMPRA`, `VENDA`, `DEVOLUCAO`, `AJUSTE`. - Histórico não deve
ser editado ou apagado para corrigir erros; correções são novas
movimentações de ajuste. - Saída não pode deixar o estoque do produto
negativo. - Venda está prevista para evolução futura.

**Estado:** geradores (Factory Method), `MovimentacaoRepository`,
`ProdutoRepository.atualizarEstoque`, tabela `movimentacoes_estoque`,
ajuste manual (`RegistrarAjusteCommand`) e consulta do histórico por
produto implementados, compilados e testados no Postman (pasta
`04 - Movimentações`, branch `feature/movimentacao`).
Ajuste em produto inativo é recusado (400).

### 4.7 Devolucao

Representa uma devolução ao fornecedor vinculada a uma compra.

**Atributos:** `id`, `compra`, `dataDevolucao`, `motivo`
(`MotivoDevolucao`), `descricao` (opcional), `status`
(`StatusDevolucao`).

**Regras:** - Status inicial `PENDENTE`. - Deve possuir pelo menos um
item. - Só pode ser concluída ou cancelada enquanto pendente. -
Conclusão gera movimentações de saída. - Cancelamento não altera
estoque. - Conclusão deve ser transacional.

### 4.8 ItemDevolucao

Representa a quantidade de um item comprado que será devolvida.

**Atributos:** `id`, `devolucao`, `itemCompra`, `quantidade`
(`BigDecimal`).

**Regras:** - Quantidade maior que zero. - O `itemCompra` deve pertencer
à mesma compra da devolução. - A soma devolvida de um item (considerando
devoluções concluídas) não pode ultrapassar a quantidade comprada. - Na
conclusão, a quantidade não pode ser maior que o estoque atual do
produto. - Conclusão da devolução gera a movimentação de saída
correspondente.

## 5. Relacionamentos

  Origem        Destino               Cardinalidade
  ------------- --------------------- ---------------
  Fornecedor    FornecedorProduto     1:N
  Produto       FornecedorProduto     1:N
  Fornecedor    Compra                1:N
  Compra        ItemCompra            1:N
  Produto       ItemCompra            1:N
  Produto       MovimentacaoEstoque   1:N
  ItemCompra    MovimentacaoEstoque   1:N (opcional)
  Compra        Devolucao             1:N
  Devolucao     ItemDevolucao         1:N
  ItemCompra    ItemDevolucao         1:N
  ItemDevolucao MovimentacaoEstoque   1:N (opcional)

O relacionamento N:N entre Fornecedor e Produto é resolvido pela
entidade associativa `FornecedorProduto`.

Representação simplificada:

``` text
Fornecedor 1 --- N FornecedorProduto N --- 1 Produto

Fornecedor 1 --- N Compra
Compra 1 --- N ItemCompra
Produto 1 --- N ItemCompra
Produto 1 --- N MovimentacaoEstoque

Compra 1 --- N Devolucao
Devolucao 1 --- N ItemDevolucao
ItemCompra 1 --- N ItemDevolucao
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

**Problema resolvido:** operações que fazem várias gravações
relacionadas precisam acontecer por completo ou não acontecer. Sem o
padrão, cada Service repetiria o controle de transação.

**Estrutura:**

-   **Command:** interface `Command` com `void executar(Connection conexao)`.
-   **Comandos concretos:**
    -   `ConfirmarRecebimentoCommand`: muda a compra para RECEBIDA,
        gera uma movimentação de ENTRADA por item e atualiza o estoque.
    -   `ConcluirDevolucaoCommand`: muda a devolução para CONCLUIDA,
        gera uma movimentação de SAIDA por item e baixa o estoque.
    -   `RegistrarAjusteCommand`: registra uma movimentação de AJUSTE
        (entrada ou saída, com motivo) e atualiza o estoque.
-   **Invoker:** `ExecutorTransacional`. Abre a conexão, desliga o
    auto-commit, executa o comando e faz `commit`, ou `rollback` em
    caso de erro.

O controle de transação fica escrito em um único lugar, e uma nova
operação composta é só um novo Command.

``` java
public void executar(Command comando) throws SQLException {
    try (Connection conexao = DatabaseConnection.getConnection()) {
        conexao.setAutoCommit(false);
        try {
            comando.executar(conexao);
            conexao.commit();
        } catch (Exception e) {
            conexao.rollback();
            throw e;
        }
    }
}
```

**Estado:** `Command`, `ExecutorTransacional` e `RegistrarAjusteCommand` implementados. `ConfirmarRecebimentoCommand` e `ConcluirDevolucaoCommand` pendentes (módulos Compra e Devolução).

### 7.5 Factory Method

**Categoria:** GoF criacional.

**Problema resolvido:** cada origem de movimentação tem regras próprias
de criação (tipo, vínculo e validações). O padrão permite acrescentar
origens novas, como VENDA, sem alterar as existentes.

**Atenção:** uma classe única com um método estático `criar(tipo,
origem)` é uma *Simple Factory*, e **não** o Factory Method do GoF. No
Factory Method, uma classe criadora abstrata declara o método de criação
e as **subclasses decidem** qual objeto criar.

**Estrutura:**

-   **Creator:** classe abstrata `GeradorMovimentacao`, com o factory
    method `criarMovimentacao(...)` e a operação comum `registrar(...)`
    (valida a quantidade e usa o objeto criado).
-   **Concrete Creators:** `GeradorEntradaCompra` (ENTRADA/COMPRA),
    `GeradorSaidaDevolucao` (SAIDA/DEVOLUCAO) e `GeradorAjuste`
    (AJUSTE, exige motivo).
-   **Product:** `MovimentacaoEstoque`.

``` java
public abstract class GeradorMovimentacao {

    protected abstract MovimentacaoEstoque criarMovimentacao(
            Produto produto, BigDecimal quantidade);

    public MovimentacaoEstoque registrar(
            Produto produto, BigDecimal quantidade) {
        validarQuantidadePositiva(quantidade);
        return criarMovimentacao(produto, quantidade);
    }
}
```

Os Commands usam os geradores: o recebimento usa
`GeradorEntradaCompra` e a conclusão da devolução usa
`GeradorSaidaDevolucao`.

**Estado:** Creator, Product e as três subclasses concretas (`GeradorEntradaCompra`, `GeradorSaidaDevolucao`, `GeradorAjuste`) implementados.

### 7.6 Contrato entre os módulos

Compra, Movimentação e Devolução são desenvolvidos por pessoas
diferentes ao mesmo tempo. Para os módulos se encaixarem, as classes e
assinaturas abaixo são **fixas**: podem ser implementadas, mas não
renomeadas nem alteradas sem combinar com a equipe.

**Já implementado (branch `feature/contrato-transacao`):**

| Classe | Papel |
|---|---|
| `command/Command` | Interface `executar(Connection)` |
| `command/ExecutorTransacional` | Abre a transação, executa, faz commit ou rollback |
| `factory/GeradorMovimentacao` | Creator abstrato com `registrar(produto, quantidade)` |
| `model/entity/MovimentacaoEstoque` | Entidade da movimentação |
| `model/enums/TipoMovimentacao` | `ENTRADA`, `SAIDA` |
| `model/enums/OrigemMovimentacao` | `COMPRA`, `VENDA`, `DEVOLUCAO`, `AJUSTE` |

**Módulo Movimentação — implementado (branch `feature/movimentacao`):**

``` java
// factory/ — subclasses de GeradorMovimentacao
new GeradorEntradaCompra(Long itemCompraId)
new GeradorSaidaDevolucao(Long itemDevolucaoId)
new GeradorAjuste(TipoMovimentacao tipo, String motivo)

// repository/MovimentacaoRepository
Long salvar(Connection conexao, MovimentacaoEstoque movimentacao)
List<MovimentacaoEstoque> listarPorProduto(Long produtoId)

// repository/ProdutoRepository (novo método)
// Soma a variação ao estoque de forma atômica. Retorna false se o
// resultado ficaria negativo (nada é alterado nesse caso).
boolean atualizarEstoque(Connection conexao, Long produtoId, BigDecimal variacao)
```

Sugestão de SQL para `atualizarEstoque`, que impede estoque negativo
mesmo com requisições simultâneas:

``` sql
UPDATE produtos
SET quantidade_estoque = quantidade_estoque + ?
WHERE id = ? AND quantidade_estoque + ? >= 0
```

**Como um Command usa o contrato** (exemplo do recebimento de compra):

``` java
public void executar(Connection conexao) throws SQLException {
    // ... valida e muda a compra para RECEBIDA usando "conexao"

    for (ItemCompra item : itens) {
        MovimentacaoEstoque mov = new GeradorEntradaCompra(item.getId())
                .registrar(item.getProduto(), item.getQuantidade());

        movimentacaoRepository.salvar(conexao, mov);

        if (!produtoRepository.atualizarEstoque(
                conexao, item.getProduto().getId(), mov.getVariacaoEstoque())) {
            throw new ConflitoException("Estoque insuficiente.");
        }
    }
}

// No Service:
executorTransacional.executar(new ConfirmarRecebimentoCommand(compraId));
```

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

### 9.4 FornecedorProduto --- implementadas, testes pendentes

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

### 9.5 Movimentações --- implementadas e testadas

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/movimentacoes/ajustes` | Registra ajuste manual de estoque |
| GET | `/api/produtos/{id}/movimentacoes` | Histórico do produto (mais recente primeiro) |

Corpo do ajuste:

``` json
{
  "produtoId": 1,
  "tipo": "SAIDA",
  "quantidade": 3,
  "motivo": "Perda por avaria"
}
```

Respostas: `201` com o `id` da movimentação; `400` para tipo, quantidade
ou motivo inválidos e para produto inativo; `404` para produto
inexistente; `409` quando a saída deixaria o estoque negativo.

Entradas por compra e saídas por devolução não têm rota própria: são
geradas pelos Commands de recebimento e de conclusão da devolução.

A rota de movimentações do produto é verificada antes da rota genérica
`/api/produtos/{id}`.

### 9.6 Compras e devoluções

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
-   `movimentacoes_estoque`

A tabela `movimentacoes_estoque` tem FK para `produtos` e `CHECK` para
tipo, origem, quantidade positiva e vínculo obrigatório por origem.
`item_compra_id` e `item_devolucao_id` ficam **sem FK** até que as
tabelas de itens de compra e de devolução existam; o `schema.sql` traz
o `ALTER TABLE` que o módulo correspondente deve acrescentar.

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
-   Métodos de Repository usados dentro de uma transação devem
    **receber a `Connection` como parâmetro**, em vez de abrir a
    própria. Caso contrário, o `rollback` não desfaz o que eles gravaram.
-   Preservar o histórico de compras, devoluções e movimentações.
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
│   ├── FornecedorProdutoController.java
│   └── MovimentacaoController.java
├── command/
│   ├── Command.java
│   ├── ExecutorTransacional.java
│   ├── RegistrarAjusteCommand.java
│   ├── ConfirmarRecebimentoCommand.java        (previsto)
│   └── ConcluirDevolucaoCommand.java           (previsto)
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
├── exception/
│   ├── ConflitoException.java
│   └── RecursoNaoEncontradoException.java
├── factory/
│   ├── GeradorMovimentacao.java
│   ├── GeradorEntradaCompra.java
│   ├── GeradorSaidaDevolucao.java
│   └── GeradorAjuste.java
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
-   Módulo `FornecedorProduto` completo (Service, Controller e rotas),
    compilado; testes no Postman pendentes.
-   Tabela `fornecedor_produto` com FKs e restrição de unicidade.
-   Módulo de Movimentação (Factory Method, repositório, atualização
    atômica de estoque, ajuste manual e histórico), compilado e testado
    no Postman.

### Próximas atividades

1.  Testar associações no Postman e abrir o PR de `FornecedorProduto`.
2.  ~~Corrigir os códigos HTTP~~ (branch `fix/status-http`).
3.  Validar CRUD de Produto com MySQL.
4.  Versionar o script do banco em `docs/schema.sql`.
5.  Implementar `ExecutorTransacional` e a interface `Command`.
6.  Implementar Compra e ItemCompra.
7.  ~~Implementar MovimentacaoEstoque e o Factory Method~~ (branch `feature/movimentacao`, testado no Postman).
8.  Implementar `ConfirmarRecebimentoCommand`.
9.  Implementar Devolucao, ItemDevolucao e `ConcluirDevolucaoCommand`.
10. Implementar e testar integração BrasilAPI.
11. Completar coleção Postman e README final.

------------------------------------------------------------------------

## 15. Evoluções futuras (fora do escopo atual)

-   Controle de estoque por lote (`LoteEstoque`) com baixa FIFO.
-   Vendas (origem `VENDA` em `MovimentacaoEstoque`, nova subclasse de
    `GeradorMovimentacao`).

## 16. Manutenção deste documento

Este arquivo deve acompanhar a evolução real do sistema. Quando uma
funcionalidade for implementada, compilada e testada, atualizar seu
estado. Rotas, atributos, regras e contratos externos só devem ser
registrados como definitivos após confirmação no código, banco ou
documentação oficial correspondente.

O objetivo é manter uma referência comum para toda a equipe e reduzir
divergências entre modelagem, código, banco e API.
