package com.booksellingapp.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping({"", "/", "/{service}"})
    public Mono<ResponseEntity<String>> fallback(@PathVariable(required = false) String service) {
        String targetService = (service == null || service.isBlank()) ? "downstream service" : service + " service";
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(targetService + " is currently unavailable")
        );
    }
}
