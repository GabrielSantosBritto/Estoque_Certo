
package br.com.estoquecerto;

import br.com.estoquecerto.infrastructure.http.ServidorHttp;

public class EstoqueCertoApplication {

    public static void main(String[] args) {

        try {
            ServidorHttp servidor = new ServidorHttp();
            servidor.iniciar();

        } catch (Exception e) {
            System.err.println(
                "Erro ao iniciar a aplicação: " + e.getMessage()
            );
        }
    }
}