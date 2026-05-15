package cl.apipedidos.pedido.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.apipedidos.pedido.client.ClienteServiceClient;
import cl.apipedidos.pedido.dto.CreateItemRequest;
import cl.apipedidos.pedido.dto.CreatePedidoRequest;
import cl.apipedidos.pedido.dto.PedidoDTO;
import cl.apipedidos.pedido.entity.Pedido;
import cl.apipedidos.pedido.entity.TipoDespacho;
import cl.apipedidos.pedido.repository.ItemPedidoRepository;
import cl.apipedidos.pedido.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ItemPedidoRepository itemPedidoRepository;

    @Mock
    private ClienteServiceClient clienteServiceClient;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void crearPedidoGuardaPedidoYCalculaMonto() {
        CreatePedidoRequest request = new CreatePedidoRequest(
            "PED-001",
            10L,
            TipoDespacho.RETIRO,
            List.of(
                new CreateItemRequest(1L, 2, new BigDecimal("1500.00")),
                new CreateItemRequest(2L, 1, new BigDecimal("500.00"))
            )
        );

        when(pedidoRepository.existsByNumeroPedido("PED-001")).thenReturn(false);
        doNothing().when(clienteServiceClient).validarCliente(10L);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(99L);
            return pedido;
        });

        PedidoDTO dto = pedidoService.crearPedido(request);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.numeroPedido()).isEqualTo("PED-001");
        assertThat(dto.monto()).isEqualByComparingTo("3500.00");
        assertThat(dto.items()).hasSize(2);
        assertThat(dto.items().get(0).subtotal()).isEqualByComparingTo("3000.00");
        assertThat(dto.items().get(1).subtotal()).isEqualByComparingTo("500.00");

        verify(clienteServiceClient).validarCliente(10L);
        verify(pedidoRepository).save(any(Pedido.class));
    }
}