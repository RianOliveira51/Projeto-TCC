package organizacao.finance.Guaxicash.service.exceptions;

public class ResourceNotFoundExeption extends RuntimeException {
    public ResourceNotFoundExeption(Object id) {

        super("Usuario não encontrado: " + id);
    }
}
