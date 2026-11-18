package br.com.estoquecerto.exception;

// Lançada quando a operação conflita com o estado atual dos dados
// (registro duplicado, recurso já desativado etc.).
// O FrontController responde com HTTP 409 Conflict.
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
