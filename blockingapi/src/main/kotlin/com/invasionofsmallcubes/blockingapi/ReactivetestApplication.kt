package com.invasionofsmallcubes.blockingapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.System.nanoTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@SpringBootApplication
open class ReactivetestApplication {
    @Bean
    open fun init(quoteRepository: QuoteRepository) = CommandLineRunner {
        for(i in 1..2000) {
            quoteRepository.save(Quote("Test quote $i.", "Me $i"));
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(ReactivetestApplication::class.java, *args)
}


class Waiter {
}

@RestController
class QuoteBlockingController @Autowired constructor(val repository: QuoteRepository) {
    val waiter = Waiter()

    @RequestMapping("/blocking/{identifier}")
    fun get(@PathVariable identifier: String): MutableIterable<Quote>? {
        val start = nanoTime();
        try {
            synchronized(waiter) {
                // kotlin concurrency thingy
                (waiter as java.lang.Object).wait(5000)
            }
        } catch (e: InterruptedException) {
        }
        val end = (nanoTime() - start) / 1000000000.0
        println("[$identifier] [$end seconds]: returning stuff really slowly :P")
        return repository.findAll()
    }
}

@RepositoryRestResource
interface QuoteRepository : CrudRepository<Quote, Long> {}

@Entity
data class Quote(
        val author: String = "",
        val quote: String = "",
        @Id @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
        var id: Long = 0)

