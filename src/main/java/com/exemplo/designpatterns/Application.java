package com.exemplo.designpatterns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// ----------------------------------------------------
// 1. PADRÃO STRATEGY: Interface e Implementações
// ----------------------------------------------------
interface DescontoStrategy {
    BigDecimal calcularDesconto(BigDecimal valorTotal);
    String getTipoCupom();
}

@Component // Singleton gerenciado pelo Spring IoC
class DescontoBlackFriday implements DescontoStrategy {
    @Override
    public BigDecimal calcularDesconto(BigDecimal valorTotal) {
        return valorTotal.multiply(new BigDecimal("0.30")); // 30% de desconto
    }

    @Override
    public String getTipoCupom() {
        return "BLACK_FRIDAY";
    }
}

@Component // Singleton gerenciado pelo Spring IoC
class DescontoPrimeiraCompra implements DescontoStrategy {
    @Override
    public BigDecimal calcularDesconto(BigDecimal valorTotal) {
        return valorTotal.multiply(new BigDecimal("0.15")); // 15% de desconto
    }

    @Override
    public String getTipoCupom() {
        return "PRIMEIRA_COMPRA";
    }
}

// Resolução dinâmica das estratégias via Injeção de Dependência do Spring
@Component
class DescontoContext {
    private final Map<String, DescontoStrategy> estrategias;

    public DescontoContext(List<DescontoStrategy> estrategiaList) {
        this.estrategias = estrategiaList.stream()
                .collect(Collectors.toMap(DescontoStrategy::getTipoCupom, Function.identity()));
    }

    public BigDecimal aplicarDesconto(String tipoCupom, BigDecimal valorTotal) {
        DescontoStrategy estrategia = estrategias.get(tipoCupom.toUpperCase());
        if (estrategia == null) {
            return BigDecimal.ZERO;
        }
        return estrategia.calcularDesconto(valorTotal);
    }
}

// ----------------------------------------------------
// 2. PADRÃO FACADE: Simplifica a orquestração do pedido
// ----------------------------------------------------
@Service
class PedidoFacade {
    private final DescontoContext descontoContext;

    public PedidoFacade(DescontoContext descontoContext) {
        this.descontoContext = descontoContext;
    }

    public ResumoPedido processarPedido(String clienteId, BigDecimal valorOriginal, String cupom) {
        BigDecimal desconto = descontoContext.aplicarDesconto(cupom, valorOriginal);
        BigDecimal valorFinal = valorOriginal.subtract(desconto);
        return new ResumoPedido(clienteId, valorOriginal, desconto, valorFinal, "PROCESSADO");
    }
}

class ResumoPedido {
    private String clienteId;
    private BigDecimal valorOriginal;
    private BigDecimal desconto;
    private BigDecimal valorFinal;
    private String status;

    public ResumoPedido(String clienteId, BigDecimal valorOriginal, BigDecimal desconto, BigDecimal valorFinal, String status) {
        this.clienteId = clienteId;
        this.valorOriginal = valorOriginal;
        this.desconto = desconto;
        this.valorFinal = valorFinal;
        this.status = status;
    }

    public String getClienteId() { return clienteId; }
    public BigDecimal getValorOriginal() { return valorOriginal; }
    public BigDecimal getDesconto() { return desconto; }
    public BigDecimal getValorFinal() { return valorFinal; }
    public String getStatus() { return status; }
}

// ----------------------------------------------------
// 3. CAMADA DE CONTROLE (REST Controller)
// ----------------------------------------------------
@RestController
@RequestMapping("/api/pedidos")
class PedidoController {
    private final PedidoFacade pedidoFacade;

    public PedidoController(PedidoFacade pedidoFacade) {
        this.pedidoFacade = pedidoFacade;
    }

    @PostMapping("/checkout")
    public ResumoPedido checkout(
            @RequestParam String clienteId,
            @RequestParam BigDecimal valor,
            @RequestParam(required = false, defaultValue = "") String cupom) {
        return pedidoFacade.processarPedido(clienteId, valor, cupom);
    }
}
