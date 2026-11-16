package br.com.estoquecerto.controller;

import br.com.estoquecerto.dto.request.ProdutoRequestDTO;
import br.com.estoquecerto.dto.response.ProdutoResponseDTO;

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
    private final ObjectMapper objectMapper;

    public FrontController() {
        this.statusController = new StatusController();
        this.produtoController = new ProdutoController();
        this.objectMapper = new ObjectMapper();
    }

    public void processarRequisicao(HttpExchange exchange)
            throws IOException {

        String metodo = exchange.getRequestMethod();
        String caminho = exchange.getRequestURI().getPath();

        try {

            if (metodo.equals("GET") &&
                    caminho.equals("/api/status")) {

                statusController.verificarStatus(exchange);
                return;
            }

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

            if (caminho.startsWith("/api/produtos/")) {

                String idTexto = caminho.substring("/api/produtos/".length());

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

            enviarJson(exchange, 404, Map.of(
                "erro", "Rota não encontrada"
            ));

        } catch (IllegalArgumentException e) {

            enviarJson(exchange, 400, Map.of(
                "erro", e.getMessage()
            ));

        } catch (SQLException e) {

            enviarJson(exchange, 500, Map.of(
                "erro", "Erro ao acessar o banco de dados."
            ));

        } catch (Exception e) {

            enviarJson(exchange, 500, Map.of(
                "erro", "Erro interno do servidor."
            ));
        }
    }

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

        Long id;

        try {
            id = Long.parseLong(idTexto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID inválido.");
        }

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

    // Atualizar produto
    private void atualizarProduto(HttpExchange exchange, String idTexto)
            throws IOException, SQLException {

        Long id = converterId(idTexto);

        ProdutoRequestDTO dto = objectMapper.readValue(
            exchange.getRequestBody(),
            ProdutoRequestDTO.class
        );

        produtoController.atualizar(id, dto);

        enviarJson(exchange, 200,
            Map.of("mensagem", "Produto atualizado com sucesso."));
    }

    // Desativar produto
    private void desativarProduto(HttpExchange exchange, String idTexto)
            throws IOException, SQLException {

        Long id = converterId(idTexto);

        produtoController.desativar(id);

        enviarJson(exchange, 200,
            Map.of("mensagem", "Produto desativado com sucesso."));
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
