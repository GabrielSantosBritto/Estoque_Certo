
package br.com.estoquecerto.controller;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StatusController {

    public void verificarStatus(HttpExchange exchange) throws IOException {

        String resposta = """
                {
                    "sistema": "EstoqueCerto",
                    "status": "online",
                    "mensagem": "API funcionando corretamente"
                }
                """;

        byte[] respostaBytes = resposta.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(200, respostaBytes.length);

        try (var corpo = exchange.getResponseBody()) {
            corpo.write(respostaBytes);
        }
    }
}