package com.example.productapi.handler;

import com.example.productapi.model.Product;
import com.example.productapi.model.ProductEvent;
import com.example.productapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Component
public class ProductHandler {
    @Autowired
    ProductRepository productRepository;

    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        Flux<Product> products = productRepository.findAll();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> existingProduct = productRepository.findById(id);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return existingProduct.flatMap(product -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromObject(product)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> saveProduct(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);
        return productMono.flatMap(product -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productRepository.save(product), Product.class));

    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> existingProductMono = productRepository.findById(id);
        Mono<Product> productMono = request.bodyToMono(Product.class);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return productMono.zipWith(existingProductMono,
                (product, existingProduct) -> new Product(existingProduct.getId(), product.getName(), product.getPrice()))
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productRepository.save(product), Product.class))
                .switchIfEmpty(notFound);

    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String id = request.pathVariable("id");

        Mono<Product> existingProductMono = productRepository.findById(id);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return existingProductMono.flatMap(existingProduct -> ServerResponse.ok().build(productRepository.delete(existingProduct)))
                .switchIfEmpty(notFound);

    }

    public Mono<ServerResponse> deleteAllProducts(ServerRequest request) {
        return ServerResponse.ok().build(productRepository.deleteAll());
    }

    public Mono<ServerResponse> getProductEvents(ServerRequest request) {
        Flux<ProductEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1))
                .map(val -> new ProductEvent(val, "Product Event"));

        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(eventsFlux, ProductEvent.class);
    }

}
