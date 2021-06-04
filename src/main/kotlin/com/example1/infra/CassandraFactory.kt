//package com.example1.infra
//
//import com.datastax.oss.driver.api.core.CqlSessionBuilder
//import io.micronaut.context.annotation.Bean
//import io.micronaut.context.annotation.Factory
//import io.micronaut.context.annotation.Value
//import io.vertx.cassandra.CassandraClient
//import io.vertx.cassandra.CassandraClientOptions
//import io.vertx.core.Vertx
//import javax.inject.Singleton
//
//@Factory
//class CassandraFactory {
//    @Bean
//    @Singleton
//    fun cassandra(vertx: Vertx, config: CassandraConfig): CassandraClient {
//        val options = CassandraClientOptions(CqlSessionBuilder()
//            .withLocalDatacenter(config.localDatacenter))
////            .setKeyspace(config.keyspace)
//        config.hosts.forEach {
//            val pair = it.split(":")
//            println(pair[0])
//            println(pair[1])
//            options.addContactPoint(pair[0], Integer.parseInt(pair[1]))
//        }
//        return CassandraClient.createShared(vertx, "sharedClientName", options)
//    }
//}
//
//@Singleton
//class CassandraConfig {
//    @Value("\${cassandra.default.basic.contact-points}")
//    lateinit var hosts: List<String>
//    @Value("\${cassandra.default.basic.session-keyspace}")
//    lateinit var keyspace: String
//    @Value("\${cassandra.default.basic.load-balancing-policy.local-datacenter}")
//    lateinit var localDatacenter: String
//    override fun toString(): String {
//        return "CassandraConfig(hosts=$hosts, keyspace='$keyspace', localDatacenter='$localDatacenter')"
//    }
//}