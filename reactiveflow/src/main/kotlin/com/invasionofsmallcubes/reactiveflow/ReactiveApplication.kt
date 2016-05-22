package com.invasionofsmallcubes.reactiveflow

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity.EMPTY
import org.springframework.http.HttpMethod.GET
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import reactor.core.publisher.Computations
import reactor.core.publisher.Flux
import reactor.core.publisher.Flux.defer
import reactor.core.publisher.Flux.fromIterable
import reactor.core.test.TestSubscriber
import java.util.stream.Collector
import java.util.stream.Collectors

@SpringBootApplication
open class ReactiveApplication {

    @Bean
    open fun quoteRepository() = QuoteRepository()
}

fun main(args: Array<String>) {
    SpringApplication.run(ReactiveApplication::class.java, *args)
}
@RestController
class ReactiveEndpoint @Autowired constructor(val quoteRepository: QuoteRepository) {
    @RequestMapping("/blocking/{identifier}")
    fun get(@PathVariable identifier: String): MutableList<in Quote> {
        return defer { fromIterable( quoteRepository.getAll(identifier)) }
                .subscribeOn(Computations.concurrent()).stream().collect(Collectors.toList());
    }
}

class QuoteRepository(val restRepository: RestTemplate = RestTemplate()) {
    fun getAll(id: String) : List<Quote> {
        return restRepository.exchange(
                "http://localhost:8088/blocking/$id", GET,
                EMPTY, object: ParameterizedTypeReference<List<Quote>>(){}).body
    }
}

data class Quote(
        val author: String = "",
        val quote: String = "",
        var id: Long = 0)