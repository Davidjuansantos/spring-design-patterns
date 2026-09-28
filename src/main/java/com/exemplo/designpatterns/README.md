# Padrões de Projeto Java com Spring Framework

Este projeto demonstra a implementação prática de Padrões de Projeto (GoF) integrados ao ecossistema Spring Boot.

## 🧠 Padrões de Projeto Utilizados

- **Strategy:** Utilizado no cálculo de descontos. As estratégias de desconto (`DescontoBlackFriday` e `DescontoPrimeiraCompra`) implementam uma interface comum, e o Spring injeta as implementações dinamicamente via `DescontoContext`.
- **Facade:** Utilizado na classe `PedidoFacade`, que abstrai a complexidade do cálculo de descontos e orquestração do fluxo do pedido para o `PedidoController`.
- **Singleton:** Aplicado nativamente através do container IoC do Spring, que gerencia os Beans (`@Component`, `@Service`, `@RestController`) como instâncias únicas no ciclo de vida da aplicação.
