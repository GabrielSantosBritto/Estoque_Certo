
package br.com.estoquecerto.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.dto.request.ProdutoRequestDTO;
import br.com.estoquecerto.dto.response.ProdutoResponseDTO;
import br.com.estoquecerto.dto.request.FornecedorRequestDTO;
import br.com.estoquecerto.dto.response.FornecedorResponseDTO;
import br.com.estoquecerto.dto.request.FornecedorProdutoRequestDTO;
import br.com.estoquecerto.dto.response.FornecedorProdutoResponseDTO;
import br.com.estoquecerto.dto.request.AjusteEstoqueRequestDTO;
import br.com.estoquecerto.dto.response.MovimentacaoResponseDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FrontController {

    private final StatusController statusController;
    private final ProdutoController produtoController;
    private final FornecedorController fornecedorController;
    private final FornecedorProdutoController fornecedorProdutoController;
    private final MovimentacaoController movimentacaoController;
    private final ObjectMapper objectMapper;

    public FrontController() {
        this.statusController = new StatusController();
        this.produtoController = new ProdutoController();
        this.fornecedorController = new FornecedorController();
        this.fornecedorProdutoController = new FornecedorProdutoController();
        this.movimentacaoController = new MovimentacaoController();
        this.objectMapper = new ObjectMapper();
    }

    public void processarRequisicao(HttpExchange exchange)
            throws IOException {

        String metodo = exchange.getRequestMethod();
        String caminho = exchange.getRequestURI().getPath();

        try {

            // Status da API
            if (metodo.equals("GET") &&
                    caminho.equals("/api/status")) {

                statusController.verificarStatus(exchange);
                return;
            }

            // Rotas de produtos
            if (metodo.equals("POST") &&
                    caminho.equals("/api/produtos")) {

                cadastrarProduto(exchange);
                return;
            }

            if (metodo.equals("GET") &&
                    caminho.equals("/api/produtos")) {

                listarProdutos(exchange);
                return;
            }

            if (metodo.equals("GET") &&
                    caminho.startsWith("/api/produtos/codigo/")) {

                String codigo = caminho.substring(
                    "/api/produtos/codigo/".length()
                );

                buscarProdutoPorCodigo(exchange, codigo);
                return;
            }

            // Histórico de movimentações de um produto
            // (precisa vir antes da rota genérica /api/produtos/{id})
            if (metodo.equals("GET") &&
                    caminho.startsWith("/api/produtos/") &&
                    caminho.endsWith("/movimentacoes")) {

                String idTexto = caminho.substring(
                    "/api/produtos/".length(),
                    caminho.length() - "/movimentacoes".length()
                );

                listarMovimentacoesDoProduto(exchange, idTexto);
                return;
            }

            if (caminho.startsWith("/api/produtos/")) {

                String idTexto = caminho.substring(
                    "/api/produtos/".length()
                );

                if (metodo.equals("GET")) {
                    buscarProdutoPorId(exchange, idTexto);
                    return;
                }

                if (metodo.equals("PUT")) {
                    atualizarProduto(exchange, idTexto);
                    return;
                }

                if (metodo.equals("DELETE")) {
                    desativarProduto(exchange, idTexto);
                    return;
                }
            }

            // Rotas de fornecedores
            if (metodo.equals("POST") &&
                    caminho.equals("/api/fornecedores")) {

                cadastrarFornecedor(exchange);
                return;
            }

            if (metodo.equals("GET") &&
                    caminho.equals("/api/fornecedores")) {

                listarFornecedores(exchange);
                return;
            }

            // Buscar fornecedor pelo CNPJ
            if (metodo.equals("GET") &&
                    caminho.startsWith("/api/fornecedores/cnpj/")) {

                String cnpj = caminho.substring(
                    "/api/fornecedores/cnpj/".length()
                );

                buscarFornecedorPorCnpj(exchange, cnpj);
                return;
            }

            // Listar produtos de um fornecedor
            // (precisa vir antes da rota genérica /api/fornecedores/{id})
            if (metodo.equals("GET") &&
                    caminho.startsWith("/api/fornecedores/") &&
                    caminho.endsWith("/produtos")) {

                String idTexto = caminho.substring(
                    "/api/fornecedores/".length(),
                    caminho.length() - "/produtos".length()
                );

                listarProdutosDoFornecedor(exchange, idTexto);
                return;
            }

            // Buscar, atualizar ou desativar fornecedor pelo ID
            if (caminho.startsWith("/api/fornecedores/")) {

                String idTexto = caminho.substring(
                    "/api/fornecedores/".length()
                );

                if (metodo.equals("GET")) {
                    buscarFornecedorPorId(exchange, idTexto);
                    return;
                }

                if (metodo.equals("PUT")) {
                    atualizarFornecedor(exchange, idTexto);
                    return;
                }

                if (metodo.equals("DELETE")) {
                    desativarFornecedor(exchange, idTexto);
                    return;
                }
            }

            // Rotas de associações fornecedor-produto
            if (metodo.equals("POST") &&
                    caminho.equals("/api/fornecedores-produtos")) {

                associarFornecedorProduto(exchange);
                return;
            }

            if (metodo.equals("GET") &&
                    caminho.equals("/api/fornecedores-produtos")) {

                listarFornecedoresProdutos(exchange);
                return;
            }

            if (caminho.startsWith("/api/fornecedores-produtos/")) {

                String idTexto = caminho.substring(
                    "/api/fornecedores-produtos/".length()
                );

                if (metodo.equals("GET")) {
                    buscarFornecedorProdutoPorId(exchange, idTexto);
                    return;
                }

                if (metodo.equals("DELETE")) {
                    excluirFornecedorProduto(exchange, idTexto);
                    return;
                }
            }

            // Rotas de movimentações de estoque
            if (metodo.equals("POST") &&
                    caminho.equals("/api/movimentacoes/ajustes")) {

                registrarAjusteEstoque(exchange);
                return;
            }

            // Rota não encontrada
            enviarJson(exchange, 404, Map.of(
                "erro", "Rota não encontrada"
            ));

        } catch (JsonProcessingException e) {

            // Corpo vazio, JSON malformado ou campo desconhecido
            enviarJson(exchange, 400, Map.of(
                "erro", "JSON inválido. Verifique o corpo da requisição."
            ));

        } catch (RecursoNaoEncontradoException e) {

            enviarJson(exchange, 404, Map.of(
                "erro", e.getMessage()
            ));

        } catch (ConflitoException e) {

            enviarJson(exchange, 409, Map.of(
                "erro", e.getMessage()
            ));

        } catch (IllegalArgumentException e) {

            enviarJson(exchange, 400, Map.of(
                "erro", e.getMessage()
            ));

        } catch (SQLException e) {

    e.printStackTrace();

    enviarJson(exchange, 500, Map.of(
        "erro", "Erro ao acessar o banco de dados."
    ));

        } catch (Exception e) {

    e.printStackTrace();

    enviarJson(exchange, 500, Map.of(
        "erro", "Erro interno do servidor."
    ));
}
    }

    // =========================
    // MÉTODOS DE PRODUTOS
    // =========================

    private void cadastrarProduto(HttpExchange exchange)
            throws IOException, SQLException {

        ProdutoRequestDTO dto = objectMapper.readValue(
            exchange.getRequestBody(),
            ProdutoRequestDTO.class
        );

        Long id = produtoController.cadastrar(dto);

        enviarJson(exchange, 201, Map.of(
            "id", id,
            "mensagem", "Produto cadastrado com sucesso."
        ));
    }

    private void listarProdutos(HttpExchange exchange)
            throws IOException, SQLException {

        List<ProdutoResponseDTO> produtos =
            produtoController.listarTodos();

        enviarJson(exchange, 200, produtos);
    }

    private void buscarProdutoPorId(
            HttpExchange exchange,
            String idTexto) throws IOException, SQLException {

        Long id = converterId(idTexto);

        Optional<ProdutoResponseDTO> produto =
            produtoController.buscarPorId(id);

        if (produto.isPresent()) {
            enviarJson(exchange, 200, produto.get());
        } else {
            enviarJson(exchange, 404, Map.of(
                "erro", "Produto não encontrado."
            ));
        }
    }

    private void buscarProdutoPorCodigo(
            HttpExchange exchange,
            String codigo) throws IOException, SQLException {

        Optional<ProdutoResponseDTO> produto =
            produtoController.buscarPorCodigo(codigo);

        if (produto.isPresent()) {
            enviarJson(exchange, 200, produto.get());
        } else {
            enviarJson(exchange, 404, Map.of(
                "erro", "Produto não encontrado."
            ));
        }
    }

    private void atualizarProduto(HttpExchange exchange, String idTexto)
            throws IOException, SQLException {

        Long id = converterId(idTexto);

        ProdutoRequestDTO dto = objectMapper.readValue(
            exchange.getRequestBody(),
            ProdutoRequestDTO.class
        );

        produtoController.atualizar(id, dto);

        enviarJson(exchange, 200, Map.of(
            "mensagem", "Produto atualizado com sucesso."
        ));
    }

    private void desativarProduto(HttpExchange exchange, String idTexto)
            throws IOException, SQLException {

        Long id = converterId(idTexto);

        produtoController.desativar(id);

        enviarJson(exchange, 200, Map.of(
            "mensagem", "Produto desativado com sucesso."
        ));
    }

    // =========================
    // MÉTODOS DE FORNECEDORES
    // =========================

    private void cadastrarFornecedor(HttpExchange exchange)
            throws IOException, SQLException {

        FornecedorRequestDTO dto = objectMapper.readValue(
            exchange.getRequestBody(),
            FornecedorRequestDTO.class
        );

        Long id = fornecedorController.cadastrar(dto);

        enviarJson(exchange, 201, Map.of(
            "id", id,
            "mensagem", "Fornecedor cadastrado com sucesso."
        ));
    }

    private void listarFornecedores(HttpExchange exchange)
            throws IOException, SQLException {

        List<FornecedorResponseDTO> fornecedores =
            fornecedorController.listarTodos();

        enviarJson(exchange, 200, fornecedores);
    }

    private void buscarFornecedorPorId(
            HttpExchange exchange,
            String idTexto) throws IOException, SQLException {

        Long id = converterId(idTexto);

        Optional<FornecedorResponseDTO> fornecedor =
            fornecedorController.buscarPorId(id);

        if (fornecedor.isPresent()) {
            enviarJson(exchange, 200, fornecedor.get());
        } else {
            enviarJson(exchange, 404, Map.of(
                "erro", "Fornecedor não encontrado."
            ));
        }
    }

    private void buscarFornecedorPorCnpj(
            HttpExchange exchange,
            String cnpj) throws IOException, SQLException {

        Optional<FornecedorResponseDTO> fornecedor =
            fornecedorController.buscarPorCnpj(cnpj);

        if (fornecedor.isPresent()) {
            enviarJson(exchange, 200, fornecedor.get());
        } else {
            enviarJson(exchange, 404, Map.of(
                "erro", "Fornecedor não encontrado."
            ));
        }
    }

    private void atualizarFornecedor(
            HttpExchange exchange,
            String idTexto) throws IOException, SQLException {

        Long id = converterId(idTexto);

        FornecedorRequestDTO dto = objectMapper.readValue(
            exchange.getRequestBody(),
            FornecedorRequestDTO.class
        );

        fornecedorController.atualizar(id, dto);

        enviarJson(exchange, 200, Map.of(
            "mensagem", "Fornecedor atualizado com sucesso."
        ));
    }

    private void desativarFornecedor(
            HttpExchange exchange,
            String idTexto) throws IOException, SQLException {

        Long id = converterId(idTexto);

        fornecedorController.desativar(id);

        enviarJson(exchange, 200, Map.of(
            "mensagem", "Fornecedor desativado com sucesso."
        ));
    }

    // =========================
    // MÉTODOS DE FORNECEDOR-PRODUTO
    // =========================

    private void associarFornecedorProduto(HttpExchange exchange)
            throws IOException, SQLException {

        FornecedorProdutoRequestDTO dto = objectMapper.readValue(
            exchange.getRequestBody(),
            FornecedorProdutoRequestDTO.class
        );

        Long id = fornecedorProdutoController.associar(dto);

        enviarJson(exchange, 201, Map.of(
            "id", id,
            "mensagem", "Produto associado ao fornecedor com sucesso."
        ));
    }

    private void listarFornecedoresProdutos(HttpExchange exchange)
            throws IOException, SQLException {

        List<FornecedorProdutoResponseDTO> associacoes =
            fornecedorProdutoController.listarTodos();

        enviarJson(exchange, 200, associacoes);
    }

    private void listarProdutosDoFornecedor(
            HttpExchange exchange,
            String idTexto) throws IOException, SQLException {

        Long fornecedorId = converterId(idTexto);

        List<FornecedorProdutoResponseDTO> associacoes =
            fornecedorProdutoController.listarPorFornecedor(fornecedorId);

        enviarJson(exchange, 200, associacoes);
    }

    private void buscarFornecedorProdutoPorId(
            HttpExchange exchange,
            String idTexto) throws IOException, SQLException {

        Long id = converterId(idTexto);

        Optional<FornecedorProdutoResponseDTO> associacao =
            fornecedorProdutoController.buscarPorId(id);

        if (associacao.isPresent()) {
            enviarJson(exchange, 200, associacao.get());
        } else {
            enviarJson(exchange, 404, Map.of(
                "erro", "Associação não encontrada."
            ));
        }
    }

    private void excluirFornecedorProduto(
            HttpExchange exchange,
            String idTexto) throws IOException, SQLException {

        Long id = converterId(idTexto);

        fornecedorProdutoController.excluir(id);

        enviarJson(exchange, 200, Map.of(
            "mensagem", "Associação excluída com sucesso."
        ));
    }

    // =========================
    // MÉTODOS DE MOVIMENTAÇÕES
    // =========================

    private void registrarAjusteEstoque(HttpExchange exchange)
            throws IOException, SQLException {

        AjusteEstoqueRequestDTO dto = objectMapper.readValue(
            exchange.getRequestBody(),
            AjusteEstoqueRequestDTO.class
        );

        Long id = movimentacaoController.registrarAjuste(dto);

        enviarJson(exchange, 201, Map.of(
            "id", id,
            "mensagem", "Ajuste de estoque registrado com sucesso."
        ));
    }

    private void listarMovimentacoesDoProduto(
            HttpExchange exchange,
            String idTexto) throws IOException, SQLException {

        Long produtoId = converterId(idTexto);

        List<MovimentacaoResponseDTO> movimentacoes =
            movimentacaoController.listarPorProduto(produtoId);

        enviarJson(exchange, 200, movimentacoes);
    }

    // =========================
    // MÉTODO DE RESPOSTA JSON
    // =========================

    private void enviarJson(
            HttpExchange exchange,
            int status,
            Object dados) throws IOException {

        byte[] resposta = objectMapper.writeValueAsBytes(dados);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, resposta.length);

        try (var corpo = exchange.getResponseBody()) {
            corpo.write(resposta);
        }
    }

    // Converter ID recebido pela URL
    private Long converterId(String idTexto) {

        try {
            return Long.parseLong(idTexto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID inválido.");
        }
    }
}
