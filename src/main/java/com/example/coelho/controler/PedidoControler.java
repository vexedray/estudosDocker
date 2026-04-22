package com.example.coelho.controler;

import com.example.coelho.DTO.PedidoDTO;
import com.example.coelho.constantes.RabbitMQConstantes;
import com.example.coelho.service.RabbitmqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pedido")
public class PedidoControler {

    @Autowired
    private RabbitmqService rabbitmqService;

    @PostMapping
    public ResponseEntity<Map<String, String>> enviarPedido(@RequestBody PedidoDTO pedido) {
        rabbitmqService.enviarMensagem(RabbitMQConstantes.FILA_PEDIDO, pedido);
        return ResponseEntity.ok(Map.of(
                "status", "sucesso",
                "mensagem", "Pedido " + pedido.id + " enviado para a fila com sucesso!"
        ));
    }
}
