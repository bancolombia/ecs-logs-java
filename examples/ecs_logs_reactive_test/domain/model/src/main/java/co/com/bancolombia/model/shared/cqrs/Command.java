package co.com.bancolombia.model.shared.cqrs;

public record Command<P, C>(P payload, C context) {
}
