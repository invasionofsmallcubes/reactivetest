package com.invasionofsmallcubes.reactiveflow

import org.slf4j.LoggerFactory
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
import org.springframework.web.context.request.async.DeferredResult
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.*
import java.util.concurrent.Executors
import java.util.function.Supplier

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

    val es = Executors.newFixedThreadPool(500);

    val logger = LoggerFactory.getLogger("ReactiveENDPOINT")

    @RequestMapping("/blocking/{identifier}")
    fun getNoBlocking(@PathVariable identifier: String): DeferredResult<List<Quote>> {
        logger.info("Starting reactive call")
        val dr = DeferredResult<List<Quote>>()
        supplyAsync( Supplier<List<Quote>> { quoteRepository.getAll(identifier) }, es)
                .whenComplete { list, throwable -> dr.setResult(list) }
        logger.info("Finishing reactive call")
        return dr
    }

    @RequestMapping("/unblocking/{identifier}")
    fun gegBlocking(@PathVariable identifier: String): List<Quote> {
        logger.info("Starting blocking call")
        val result = quoteRepository.getAll(identifier)
        logger.info("Finishing blocking call")
        return result
    }
}

class QuoteRepository(val restRepository: RestTemplate = RestTemplate()) {
    val logger = LoggerFactory.getLogger("QuoteREPO")

    fun getAll(id: String) : List<Quote> {
        logger.info("Starting heavy call")
        val result = restRepository.exchange(
                "http://localhost:8088/blocking/$id", GET,
                EMPTY, object: ParameterizedTypeReference<List<Quote>>(){}).body
        logger.info("Finishing heavy call")
        return result
    }
}

data class Quote(
        val author: String = "",
        val quote: String = "",
        var id: Long = 0)