package br.com.estoquecerto.infrastructure.external;

import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.exception.ServicoExternoIndisponivelException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BrasilApiClient {

    private static final String BASE_URL = "https://brasilapi.com.br/api/cnpj/v1/";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BrasilApiClient() {
        this(
            HttpClient.newBuilder().connectTimeout(TIMEOUT).build(),
            new ObjectMapper()
        );
    }

    public BrasilApiClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public JsonNode buscarCnpj(String cnpjDigitos) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + cnpjDigitos))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            int status = response.statusCode();

            if (status == 200) {
                return objectMapper.readTree(response.body());
            }

            if (status == 404) {
                throw new RecursoNaoEncontradoException("CNPJ não encontrado na BrasilAPI.");
            }

            if (status == 400 || status == 422) {
                throw new IllegalArgumentException("CNPJ inválido ou rejeitado pela consulta externa.");
            }

            if (status >= 500) {
                throw new ServicoExternoIndisponivelException(
                        "Serviço de consulta de CNPJ indisponível no momento."
                );
            }

            throw new ServicoExternoIndisponivelException(
                    "Resposta inesperada do serviço de consulta de CNPJ."
            );
        } catch (RecursoNaoEncontradoException | IllegalArgumentException | ServicoExternoIndisponivelException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicoExternoIndisponivelException(
                    "Consulta de CNPJ interrompida. Tente novamente.",
                    e
            );
        } catch (IOException e) {
            throw new ServicoExternoIndisponivelException(
                    "Falha ou tempo esgotado ao consultar a BrasilAPI.",
                    e
            );
        }
    }
}