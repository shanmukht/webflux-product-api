package com.example.productapi;

import com.example.productapi.handler.ProductHandler;
import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class ProductApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApiApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ProductRepository repository) {
        return args -> {
            Flux<Product> productFlux = Flux.just(
                    new Product(null, "Big Latte", 2.99),
                    new Product(null, "Big Decaf", 2.49),
                    new Product(null, "Green Tea", 1.99)
            ).flatMap(repository::save);

            productFlux.thenMany(repository.findAll()).subscribe(System.out::println);

        };

    }

    @Bean
    RouterFunction<ServerResponse> routes(ProductHandler handler) {
        String context = "/data";
        return route(GET(context).and(accept(MediaType.APPLICATION_JSON)), handler::getAllProducts)
                .andRoute(POST(context).and(contentType(MediaType.APPLICATION_JSON)), handler::saveProduct)
                .andRoute(DELETE(context).and(accept(MediaType.APPLICATION_JSON)), handler::deleteAllProducts)
                .andRoute(GET(context + "/events").and(accept(MediaType.TEXT_EVENT_STREAM)), handler::getProductEvents)
                .andRoute(GET(context + "/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::getProduct)
                .andRoute(PUT(context + "/{id}").and(contentType(MediaType.APPLICATION_JSON)), handler::updateProduct)
                .andRoute(DELETE(context + "/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::deleteProduct);
    }

}
