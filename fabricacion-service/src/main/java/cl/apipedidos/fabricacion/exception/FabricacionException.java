package cl.apipedidos.fabricacion.exception;

public class FabricacionException extends RuntimeException {
    public FabricacionException(String mensaje) { super(mensaje); }
    public FabricacionException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
