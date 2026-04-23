package cl.apipedidos.cliente.gui;

import java.awt.EventQueue;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RegistroUsuariosStartupListener {

    @EventListener(ApplicationReadyEvent.class)
    public void abrirFormularioRegistro() {
        EventQueue.invokeLater(() -> new RegistroUsuarios().setVisible(true));
    }
}