package br.com.estoquecerto.exception;

// Lançada quando o recurso solicitado não existe.
// O FrontController responde com HTTP 404 Not Found.
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
