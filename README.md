# EstoqueCerto

## 1. Sobre o projeto

O **EstoqueCerto** é uma API REST de gestão de compras e controle de estoque, feita para ajudar micro e pequenos comerciantes a organizar a compra de mercadorias e saber, a qualquer momento, quanto têm de cada produto e por que esse número mudou.

A aplicação reúne fornecedores, produtos, compras, devoluções e o histórico de movimentações de estoque. O estoque é atualizado automaticamente quando o recebimento de uma compra é confirmado.

Projeto final da disciplina **Padrões de Projeto em Aplicações Web**, desenvolvido em **Java puro, sem frameworks**, como exigido pela disciplina.

## 2. Integrantes da equipe

| Integrante | Principais contribuições |
|---|---|
| Agenor Santos Britto | Fornecedor, FornecedorProduto, tratamento centralizado de exceções, módulo de Compra e estrutura do padrão Command |
| Gabriel Santos Britto | Estrutura inicial, Movimentação de estoque e padrão Factory Method, módulo de Devolução |
| Thailon Da Paixão Ferreira | Integração com a BrasilAPI, enumeração `MotivoDevolucao`, README |

## 3. Problema e justificativa

Micro e pequenos comerciantes costumam controlar compras e estoque em cadernos ou planilhas soltas. Isso dificulta acompanhar o que foi pedido, o que chegou e quanto resta de cada produto, e abre espaço para divergências nas quantidades.

O EstoqueCerto centraliza esse controle numa aplicação em que **o estoque nunca é editado diretamente**: ele só muda por uma movimentação registrada (entrada ou saída), que fica no histórico para sempre. Assim, todo número de estoque pode ser explicado.

## 4. Objetivo do projeto

Desenvolver uma API que permita ao comerciante:

- cadastrar fornecedores e produtos e definir quais produtos cada fornecedor vende;
- registrar compras e dar entrada automática no estoque ao confirmar o recebimento;
- devolver mercadorias ao fornecedor, com baixa automática no estoque;
- corrigir o estoque com ajustes justificados;
- consultar o histórico completo de movimentações de cada produto.

## 5. Tecnologias utilizadas

- **Java 21** (ou mais novo): linguagem de programação.
- **JDK HttpServer** (`com.sun.net.httpserver`): servidor HTTP do próprio Java.
- **JDBC**: acesso ao banco de dados com SQL escrito à mão (`PreparedStatement`).
- **Jackson**: conversão entre objetos Java e JSON.
- **`java.net.http.HttpClient`**: cliente HTTP do próprio Java, usado na integração com a BrasilAPI.
- **MySQL 8**: sistema gerenciador de banco de dados.
- **Maven**: gerenciamento de dependências e compilação.
- **Postman / Newman**: testes das requisições HTTP.

Não são usados Spring, Hibernate/JPA ou qualquer outro framework. Os papéis que um framework cumpriria (roteamento, conversão de dados, acesso ao banco, transações) foram implementados com os padrões de projeto da seção 10.

## 6. API externa

O projeto usa a **BrasilAPI**, uma API pública e gratuita que fornece os dados cadastrais de empresas a partir do CNPJ. A consulta serve para **conferir** os dados de um fornecedor antes de cadastrá-lo; ela nunca cadastra nada sozinha.

**Endpoint da API externa:**

`GET https://brasilapi.com.br/api/cnpj/v1/{cnpj}`

**Endpoint disponibilizado pelo EstoqueCerto:**

`GET /api/cnpj/{cnpj}`

Aceita o CNPJ com ou sem pontuação e devolve os dados já com os nomes de campo do fornecedor (razão social, endereço, telefone etc.).

| Situação | Resposta |
|---|---|
| CNPJ encontrado | `200 OK` com os dados |
| Formato inválido ou rejeitado pela BrasilAPI | `400 Bad Request` |
| CNPJ não encontrado | `404 Not Found` |
| BrasilAPI fora do ar, demora acima de 10 s ou erro interno | `503 Service Unavailable` |

A comunicação fica isolada na classe `BrasilApiClient`. Documentação da API: https://brasilapi.com.br/docs

## 7. Requisitos funcionais

| Código | Requisito |
|---|---|
| RF01 | Cadastrar, consultar, atualizar e desativar fornecedores. |
| RF02 | Consultar dados de empresas na BrasilAPI pelo CNPJ. |
| RF03 | Cadastrar, consultar, atualizar e desativar produtos, com código gerado automaticamente. |
| RF04 | Associar produtos aos fornecedores que os vendem. |
| RF05 | Registrar compras vinculadas a um fornecedor, com um ou mais itens. |
| RF06 | Confirmar o recebimento integral de uma compra ou cancelar uma compra pendente. |
| RF07 | Atualizar automaticamente o estoque ao confirmar o recebimento, registrando uma movimentação de entrada por item. |
| RF08 | Consultar o histórico de movimentações de estoque de cada produto. |
| RF09 | Registrar ajustes manuais de estoque (entrada ou saída) com motivo obrigatório. |
| RF10 | Registrar devoluções ao fornecedor vinculadas à compra; concluir a devolução gera a saída do estoque, e cancelá-la não movimenta nada. |

## 8. Regras de negócio

### Fornecedores e produtos

- O CNPJ de um fornecedor é obrigatório e único.
- O código do produto é gerado pelo sistema no formato `PROD-0001`.
- O produto começa com estoque zero, e a atualização cadastral nunca altera o estoque.
- Um fornecedor pode vender vários produtos, e um produto pode ter vários fornecedores. A mesma associação não pode ser cadastrada duas vezes.
- Fornecedores e produtos não são excluídos: são **desativados**, preservando o histórico.
- Fornecedor ou produto inativo não pode ser associado nem usado em novas compras.

### Compras e recebimento

- A compra pertence a um fornecedor ativo e tem pelo menos um item.
- Só entram na compra produtos ativos e **associados àquele fornecedor**.
- O mesmo produto não pode se repetir na mesma compra.
- Quantidade e preço unitário maiores que zero; frete e desconto opcionais e não negativos.
- O total é **calculado** (itens + frete − desconto), nunca armazenado, e não pode ser negativo.
- A data da compra não pode estar no futuro.
- A compra começa `PENDENTE`. Só uma compra pendente pode ser recebida ou cancelada.
- O recebimento é integral e acontece numa única transação: muda o status para `RECEBIDA`, registra uma entrada por item e soma o estoque. Uma compra não pode ser recebida duas vezes.

### Controle de estoque

- Toda alteração de estoque gera uma movimentação com tipo (`ENTRADA` ou `SAIDA`) e origem (`COMPRA`, `DEVOLUCAO` ou `AJUSTE`).
- A quantidade de uma movimentação é sempre positiva.
- Nenhuma saída pode deixar o estoque negativo.
- Movimentações não são editadas nem apagadas. Correções são feitas com um novo ajuste, que exige motivo.

### Devoluções

- Só uma compra `RECEBIDA` aceita devolução. Uma compra pendente se resolve com o cancelamento.
- A devolução tem pelo menos um item, e cada item precisa pertencer à compra da devolução.
- O motivo é obrigatório. Quando o motivo é `OUTROS`, a descrição também é.
- A soma devolvida de um item não pode ultrapassar a quantidade comprada. Isso é verificado também **na conclusão**, dentro da transação, para que duas devoluções simultâneas não passem juntas do limite.
- Produto ou fornecedor inativo pode receber devolução, porque ela resolve uma compra já feita.
- A devolução começa `PENDENTE`. Concluir gera a saída do estoque; cancelar não movimenta nada.

## 9. Arquitetura da aplicação

O sistema é organizado em camadas, e cada camada só conhece a de baixo:

- **Front Controller:** ponto único de entrada das requisições HTTP. Roteia cada requisição e traduz as exceções em códigos de status.
- **Controller:** recebe os dados de cada recurso e chama o Service.
- **DTO:** representa os dados recebidos e enviados pela API.
- **Mapper:** converte entidades em DTOs.
- **Service:** concentra as regras de negócio e as validações.
- **Command:** encapsula as operações que gravam várias linhas relacionadas, executadas numa transação.
- **Factory:** cria as movimentações de estoque.
- **Repository:** acesso ao banco de dados via JDBC.
- **Entity:** entidades e enumerações do domínio.
- **Client:** comunicação com a API externa (`BrasilApiClient`).

```text
src/main/java/br/com/estoquecerto/
├── command/          Command + ExecutorTransacional
├── controller/       FrontController + controllers por recurso
├── dto/              Objetos de entrada (request) e saída (response)
├── exception/        Exceções traduzidas em status HTTP pelo FrontController
├── factory/          Factory Method das movimentações
├── infrastructure/   Conexão com o banco, servidor HTTP e cliente da BrasilAPI
├── mapper/           Conversão entre entidades e DTOs
├── model/            Entidades e enums do domínio
├── repository/       Acesso ao banco (DAO / Repository)
└── service/          Regras de negócio
```

### 9.1. Rotas da API

| Recurso | Rotas |
|---|---|
| Status | `GET /api/status` |
| Fornecedores | `POST/GET /api/fornecedores` · `GET/PUT/DELETE /api/fornecedores/{id}` · `GET /api/fornecedores/cnpj/{cnpj}` · `GET /api/fornecedores/{id}/produtos` |
| Produtos | `POST/GET /api/produtos` · `GET/PUT/DELETE /api/produtos/{id}` · `GET /api/produtos/codigo/{codigo}` · `GET /api/produtos/{id}/movimentacoes` |
| Fornecedor × Produto | `POST/GET /api/fornecedores-produtos` · `GET/DELETE /api/fornecedores-produtos/{id}` |
| Movimentações | `POST /api/movimentacoes/ajustes` |
| Compras | `POST/GET /api/compras` · `GET /api/compras/{id}` · `POST /api/compras/{id}/recebimento` · `POST /api/compras/{id}/cancelamento` · `GET /api/compras/{id}/devolucoes` |
| Devoluções | `POST/GET /api/devolucoes` · `GET /api/devolucoes/{id}` · `POST /api/devolucoes/{id}/conclusao` · `POST /api/devolucoes/{id}/cancelamento` |
| Consulta de CNPJ | `GET /api/cnpj/{cnpj}` |

Erros respondem sempre com JSON `{"erro": "..."}` e o status correspondente:

| Situação | Status |
|---|---|
| JSON inválido, dado inválido ou regra de negócio violada | `400 Bad Request` |
| Recurso inexistente | `404 Not Found` |
| Duplicidade ou estado incompatível (ex.: receber compra já recebida) | `409 Conflict` |
| BrasilAPI indisponível | `503 Service Unavailable` |

## 10. Padrões de projeto

### 10.1. Padrões arquiteturais

**Front Controller**

Implementado na classe `controller/FrontController.java`. Todas as requisições HTTP entram por ela, que faz o roteamento para o controller do recurso e o **tratamento centralizado de exceções**: os Services lançam exceções de domínio (`RecursoNaoEncontradoException`, `ConflitoException`, `ServicoExternoIndisponivelException`) e o FrontController as traduz em 404, 409 e 503. Nenhum Service conhece HTTP.

**DTO (Data Transfer Object)**

Pacotes `dto/request` e `dto/response`. A API nunca expõe as entidades: recebe e devolve DTOs, convertidos pelos `mapper/`. Exemplo: o `CompraResponseDTO` traz o subtotal e o total calculados, que não existem no banco, e o `CompraRequestDTO` não aceita `id` nem `status`.

Exemplos: `CompraRequestDTO`, `CompraResponseDTO`, `ProdutoRequestDTO`, `FornecedorResponseDTO`.

**DAO / Repository**

Pacote `repository/`. Todo o SQL fica nos repositories (por exemplo, `CompraRepository`, `MovimentacaoRepository`), escrito com JDBC. Os Services pedem os dados sem conhecer SQL. Os métodos usados dentro de uma transação recebem a `Connection` como parâmetro, para participarem do mesmo commit ou rollback.

### 10.2. Padrões GoF

**Command** (pacote `command/`)

- **Problema:** operações como receber uma compra gravam várias linhas relacionadas (status, movimentações, estoque). Se uma falhar, as outras não podem ficar gravadas.
- **Solução:** cada operação composta é um *Command* com o método `executar(Connection)`. O `ExecutorTransacional` é o *Invoker*: abre a transação, executa o comando e faz commit, ou rollback em caso de erro.
- **Classes:** interface `Command`; Invoker `ExecutorTransacional`; comandos concretos `CadastrarCompraCommand`, `CancelarCompraCommand`, `ConfirmarRecebimentoCommand`, `CadastrarDevolucaoCommand`, `CancelarDevolucaoCommand`, `ConcluirDevolucaoCommand` e `RegistrarAjusteCommand`.
- **Ganho:** o controle de transação está escrito num único lugar. Uma operação nova é só um comando novo.

**Factory Method** (pacote `factory/`)

- **Problema:** cada origem de movimentação tem regras próprias: a de compra aponta para o item comprado, a de devolução para o item devolvido, e o ajuste exige motivo.
- **Solução:** `GeradorMovimentacao` é o *Creator* abstrato. Ele declara o factory method `criarMovimentacao()` e a operação comum `registrar()`, que valida a quantidade e impede saída maior que o estoque. Cada subclasse decide qual movimentação criar.
- **Classes:** Creator `GeradorMovimentacao`; Concrete Creators `GeradorEntradaCompra`, `GeradorSaidaDevolucao` e `GeradorAjuste`; Product `MovimentacaoEstoque`.
- **Ganho:** uma origem nova (por exemplo, venda) é uma subclasse nova, sem alterar as existentes.

**Os dois padrões juntos:** ao receber uma compra, o `ConfirmarRecebimentoCommand` usa o `GeradorEntradaCompra` para criar a movimentação de cada item. O Command garante a transação; o Factory Method garante que cada movimentação seja criada corretamente.

## 11. Escopo do projeto

O sistema contempla o gerenciamento de:

- Fornecedores;
- Produtos;
- Associação entre fornecedores e produtos;
- Compras e itens de compra;
- Movimentações de estoque (entradas, saídas e ajustes);
- Devoluções e itens de devolução;
- Consulta de CNPJ na BrasilAPI.

O controle de estoque é feito **por produto**, integrado ao recebimento das compras e às devoluções.

**Fora do escopo:** o controle por lote com baixa FIFO e o módulo de vendas foram retirados do escopo por causa do prazo. A estrutura já prevê as duas evoluções: a origem `VENDA` existe na enumeração e seria uma nova subclasse de `GeradorMovimentacao`.

## 12. Documentação

Na pasta [`docs`](docs):

- [`DOCUMENTACAO.md`](docs/DOCUMENTACAO.md): documentação técnica completa (entidades, regras, decisões e rotas).
- [`schema.sql`](docs/schema.sql): script de criação do banco de dados.

Na pasta [`docs/diagramas`](docs/diagramas), cada diagrama em imagem e em arquivo editável do [draw.io](https://app.diagrams.net):

| Diagrama | Imagem | Editável |
|---|---|---|
| Diagrama entidade-relacionamento (8 tabelas) | [DER estoque_certo.drawio.png](docs/diagramas/DER%20estoque_certo.drawio.png) | [EstoqueCerto-DER.drawio](docs/diagramas/EstoqueCerto-DER.drawio) |
| Diagrama de classes (domínio, padrões GoF e camadas) | [Diagrama de classe estoque_certo.drawio.png](docs/diagramas/Diagrama%20de%20classe%20estoque_certo.drawio.png) | [EstoqueCerto-Classes.drawio](docs/diagramas/EstoqueCerto-Classes.drawio) |
| Diagrama de componentes | [Diagrama de componentes.drawio.png](docs/diagramas/Diagrama%20de%20componentes.drawio.png) | [EstoqueCerto-Componentes.drawio](docs/diagramas/EstoqueCerto-Componentes.drawio) |

## 13. Execução do projeto

### Pré-requisitos

- **JDK 21** ou mais novo (`java -version`)
- **Maven** (`mvn -version`)
- **MySQL 8**

### 1. Criar o banco

Abra o terminal **na pasta do projeto** e entre no MySQL. No PowerShell, o `&` antes do caminho é obrigatório:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

Dentro do MySQL, execute o script. Ele cria o banco `estoquecerto` e as 8 tabelas, e pode ser executado mais de uma vez sem apagar dados:

```sql
source docs/schema.sql
```

### 2. Configurar as variáveis de ambiente

A conexão com o banco é lida de três variáveis de ambiente. **Defina-as no mesmo terminal em que vai iniciar o servidor**, porque elas valem só para aquela janela.

PowerShell (Windows):

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/estoquecerto"
$env:DB_USER = "root"
$env:DB_PASSWORD = "sua_senha"
```

Linux / macOS:

```bash
export DB_URL="jdbc:mysql://localhost:3306/estoquecerto"
export DB_USER="root"
export DB_PASSWORD="sua_senha"
```

### 3. Iniciar o servidor

```powershell
mvn clean compile exec:java
```

Quando aparecer `Endereço: http://localhost:8080`, a API está no ar. Para conferir, abra `http://localhost:8080/api/status` no navegador.

> Se aparecer o erro **"As variáveis de ambiente do banco não foram configuradas"**, o servidor foi iniciado num terminal sem as variáveis do passo 2.

## 14. Testes da API

A coleção [`postman/EstoqueCerto.postman_collection.json`](postman/EstoqueCerto.postman_collection.json) tem **147 requisições e 263 testes automatizados**, em pastas por módulo: Status, Fornecedores, Produtos, Fornecedor x Produto, Movimentações, Compras, Consulta CNPJ e Devoluções.

Os testes cobrem os casos de sucesso e os de erro (400, 404, 409 e 503). A coleção **cria os próprios dados**, então pode ser executada várias vezes no mesmo banco.

Com o servidor rodando:

- **Postman:** Import → selecionar o arquivo → executar as requisições na ordem, ou **Run collection** para todas.
- **Terminal** (sem limite de execuções, requer Node.js):

  ```powershell
  npx newman run postman/EstoqueCerto.postman_collection.json
  ```

  Resultado esperado: **263 testes, 0 falhas**.

Os testes da pasta `06 - Consulta CNPJ` consultam a BrasilAPI e precisam de acesso à internet.

## 15. Versionamento

O código-fonte é versionado com Git e hospedado num repositório público no GitHub.

Cada funcionalidade foi desenvolvida numa branch própria (`feature/fornecedores`, `feature/compra`, `feature/movimentacao`, `feature/devolucao`, `feature/brasilapi` e outras) e incorporada à `master` por Pull Request, revisado por outro integrante antes do merge.

---

**EstoqueCerto — Organização de compras e controle de estoque para pequenos comércios.**
