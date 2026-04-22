package com.example.coelho.controler;

import com.example.coelho.DTO.EstoqueDTO;
import com.example.coelho.constantes.RabbitMQConstantes;
import com.example.coelho.service.RabbitmqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "estoque")
public class EstoqueControler {

    @Autowired
    private RabbitmqService rabbitmqService;
    @PutMapping
    public ResponseEntity<Void> alterarEstoque(@RequestBody EstoqueDTO estoqueDTO){
        this.rabbitmqService.enviarMensagem(RabbitMQConstantes.FILA_ESTOQUE, estoqueDTO);
        return ResponseEntity.ok().build();
    }
}
