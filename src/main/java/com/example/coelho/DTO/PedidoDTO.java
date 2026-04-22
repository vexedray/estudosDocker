package com.example.coelho.DTO;

import java.io.Serializable;
import java.math.BigDecimal;

public class PedidoDTO implements Serializable {

    public String id;
    public String cliente;
    public BigDecimal valor;
}
