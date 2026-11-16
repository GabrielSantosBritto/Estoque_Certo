package br.com.estoquecerto.infrastructure.http;

import br.com.estoquecerto.controller.FrontController;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ServidorHttp {

    private HttpServer servidor;

    public void iniciar() throws IOException {

        servidor = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        FrontController frontController = new FrontController();

        servidor.createContext(
            "/",
            frontController::processarRequisicao
        );

        servidor.start();

        System.out.println("Servidor HTTP iniciado!");
        System.out.println("Endereço: http://localhost:8080");
        System.out.println("Front Controller ativo!");
    }
}
