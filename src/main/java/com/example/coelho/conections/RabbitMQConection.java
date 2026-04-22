package com.example.coelho.conections;

import com.example.coelho.constantes.RabbitMQConstantes;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;



@Component
public class RabbitMQConection {

    private static final String NOME_EXCHANGE = "amq.direct";

    private AmqpAdmin amqpAdmin;

    private RabbitMQConection(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;

    }

    private Queue fila(String nomeFila) {
        return new Queue(nomeFila, true, false, false);
    }
    private DirectExchange trocaDireta(){
        return new DirectExchange(NOME_EXCHANGE);
    }

    private Binding relacionamento(Queue fila, DirectExchange troca){
        return new Binding(fila.getName(), Binding.DestinationType.QUEUE, troca.getName(), fila.getName(),null);
    }

    @PostConstruct
    private void adiciona(){
        Queue filaEstoque = this.fila(RabbitMQConstantes.FILA_ESTOQUE);
        Queue filaPreco = this.fila(RabbitMQConstantes.FILA_PRECO);
        Queue filaPedido = this.fila(RabbitMQConstantes.FILA_PEDIDO);

        DirectExchange troca = this.trocaDireta();

        Binding ligacaoEstoque = this.relacionamento(filaEstoque, troca);
        Binding ligacaoPreco = this.relacionamento(filaPreco, troca);
        Binding ligacaoPedido = this.relacionamento(filaPedido, troca);

        this.amqpAdmin.declareQueue(filaEstoque);
        this.amqpAdmin.declareQueue(filaPreco);
        this.amqpAdmin.declareQueue(filaPedido);
        this.amqpAdmin.declareExchange(troca);
        this.amqpAdmin.declareBinding(ligacaoPreco);
        this.amqpAdmin.declareBinding(ligacaoEstoque);
        this.amqpAdmin.declareBinding(ligacaoPedido);
    }
}
