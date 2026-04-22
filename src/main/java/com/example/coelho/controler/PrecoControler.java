package com.example.coelho.controler;

import com.example.coelho.DTO.PrecoDTO;
import com.example.coelho.constantes.RabbitMQConstantes;
import com.example.coelho.service.RabbitmqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "preco")
public class PrecoControler {

    @Autowired
    private RabbitmqService rabbitmqService;
    @PutMapping
    public ResponseEntity<Void> alterarPreco(@RequestBody PrecoDTO precoDTO){
        this.rabbitmqService.enviarMensagem(RabbitMQConstantes.FILA_PRECO, precoDTO);
        return ResponseEntity.ok().build();
    }

}
