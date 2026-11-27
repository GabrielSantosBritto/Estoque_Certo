# EstoqueCerto

API REST para controle de estoque de um pequeno comércio: cadastro de fornecedores e produtos, compras com entrada automática no estoque, devoluções ao fornecedor, ajustes manuais com histórico completo de movimentações e consulta de CNPJ na BrasilAPI.

Projeto final da disciplina **Padrões de Projeto em Aplicações Web** (IFTO), desenvolvido em **Java puro, sem frameworks**, por exigência da disciplina.

## Domínio do problema

Um comércio precisa saber, a qualquer momento, quanto tem de cada produto e **por que** esse número mudou. No EstoqueCerto:

- O estoque de um produto **nunca é editado diretamente**. Ele só muda por uma **movimentação** registrada (entrada ou saída), que fica no histórico para sempre.
- Uma **compra** é cadastrada como `PENDENTE`. Quando a mercadoria chega, a compra é **recebida**: cada item vira uma movimentação de entrada e o estoque é somado, tudo numa única transação.
- Uma **devolução** ao fornecedor só pode ser feita sobre uma compra recebida. Ao ser **concluída**, gera movimentações de saída e baixa o estoque, sem deixar devolver mais do que foi comprado.
- Um produto só pode entrar numa compra se estiver **associado ao fornecedor** daquela compra.
- O CNPJ de um fornecedor pode ser consultado na **BrasilAPI** para conferir os dados antes do cadastro.

Detalhes de regras, entidades e decisões estão em [`docs/DOCUMENTACAO.md`](docs/DOCUMENTACAO.md).

## Onde está cada padrão

| Padrão | Onde | Para que serve no projeto |
|---|---|---|
| **Front Controller** | `controller/FrontController.java` | Ponto único de entrada de todas as requisições HTTP. Faz o roteamento e o **tratamento centralizado de exceções**, traduzindo cada uma no status HTTP correto (400, 404, 409, 503). |
| **DTO** | `dto/request/` e `dto/response/` | Separa o JSON da API das entidades do domínio. A conversão é feita pelos `mapper/`. |
| **DAO / Repository** | `repository/` | Todo o acesso ao banco via JDBC fica aqui. Os Services não conhecem SQL. |
| **Command** (GoF) | `command/` | Cada operação que grava várias linhas relacionadas é um comando (`ConfirmarRecebimentoCommand`, `ConcluirDevolucaoCommand`, `RegistrarAjusteCommand` e outros). O **`ExecutorTransacional`** (Invoker) executa qualquer comando dentro de uma transação: confirma tudo ou desfaz tudo. |
| **Factory Method** (GoF) | `factory/` | `GeradorMovimentacao` declara o método de criação, e cada subclasse (`GeradorEntradaCompra`, `GeradorSaidaDevolucao`, `GeradorAjuste`) decide o tipo, a origem e o vínculo da movimentação. Uma nova origem (por exemplo, venda) é só uma nova subclasse. |

**Os dois padrões GoF trabalham juntos:** ao receber uma compra, o `ConfirmarRecebimentoCommand` usa o `GeradorEntradaCompra` para criar a movimentação de cada item. O Command garante a transação; o Factory Method garante que cada movimentação seja criada corretamente.

## Diagramas

Em [`docs/diagramas/`](docs/diagramas) (abrir com [draw.io](https://app.diagrams.net)):

- **`EstoqueCerto-DER.drawio`**: diagrama entidade-relacionamento das 8 tabelas.
- **`EstoqueCerto-Classes.drawio`**: diagrama de classes em 3 páginas (domínio, padrões Command e Factory Method, camadas do módulo de Compra).
- **`EstoqueCerto-Componentes.drawio`**: componentes da aplicação e sistemas externos.

## Tecnologias

Java 21+ · Maven · MySQL 8 · JDBC · Jackson · `com.sun.net.httpserver` (servidor HTTP do JDK) · `java.net.http.HttpClient` (BrasilAPI) · Postman / Newman

## Como executar

### 1. Pré-requisitos

- **JDK 21** ou mais recente (`java -version`)
- **Maven** (`mvn -version`)
- **MySQL 8**

### 2. Criar o banco

Entre no MySQL e execute o script. Ele cria o banco `estoquecerto` e todas as tabelas, e pode ser executado mais de uma vez sem apagar dados.

```sql
source C:/caminho/para/Estoque_Certo/docs/schema.sql
```

> No caminho do `source`, use barras normais (`/`), mesmo no Windows.
>
> No PowerShell, o executável do MySQL precisa do `&` antes do caminho:
> `& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p`

### 3. Configurar as variáveis de ambiente

A aplicação lê a conexão com o banco de três variáveis de ambiente. **Defina-as no mesmo terminal em que vai iniciar o servidor.**

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

> Se aparecer o erro **"As variáveis de ambiente do banco não foram configuradas"**, o servidor foi iniciado num terminal sem essas variáveis.

### 4. Iniciar o servidor

```powershell
mvn clean compile exec:java
```

A API fica disponível em `http://localhost:8080`. Para conferir: `GET http://localhost:8080/api/status`.

## Testes

A coleção [`postman/EstoqueCerto.postman_collection.json`](postman/EstoqueCerto.postman_collection.json) tem **147 requisições e 263 testes automatizados**, organizados por módulo. Ela cobre os casos de sucesso e os de erro (400, 404, 409 e 503) e **cria os próprios dados**, então pode ser executada várias vezes no mesmo banco.

Com o servidor rodando:

- **Postman:** Import → selecionar o arquivo → nos três pontos da coleção, **Run collection**.
- **Terminal (sem limite de execuções, requer Node.js):**

  ```powershell
  npx newman run postman/EstoqueCerto.postman_collection.json
  ```

Os testes da pasta `06 - Consulta CNPJ` consultam a BrasilAPI e precisam de acesso à internet.

## Rotas

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

Exemplos de corpo e todas as respostas de erro estão na seção 9 da [documentação](docs/DOCUMENTACAO.md).

## Estrutura

```text
src/main/java/br/com/estoquecerto/
├── command/          Command + ExecutorTransacional (GoF)
├── controller/       FrontController + controllers por recurso
├── dto/              Objetos de entrada (request) e saída (response)
├── exception/        Exceções traduzidas em status HTTP pelo FrontController
├── factory/          Factory Method das movimentações (GoF)
├── infrastructure/   Conexão com o banco, servidor HTTP e cliente da BrasilAPI
├── mapper/           Conversão entre DTOs e entidades
├── model/            Entidades e enums do domínio
├── repository/       Acesso ao banco (DAO / Repository)
└── service/          Regras de negócio
```

## Equipe

| Integrante | Principais contribuições |
|---|---|
| Agenor Britto | Fornecedor, FornecedorProduto, tratamento de exceções, módulo de Compra, estrutura do Command |
| Gabriel Santos Britto | Estrutura inicial, Movimentação e Factory Method, módulo de Devolução |
| Thailon Ferreira | Integração com a BrasilAPI, `MotivoDevolucao`, README |
