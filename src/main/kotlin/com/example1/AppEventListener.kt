package com.example1

import com.example1.infra.CassandraConfig
import com.example1.infra.registerLocalCodec
import io.github.crabzilla.stack.EventsPublisherVerticle
import io.micronaut.context.annotation.Context
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.vertx.cassandra.CassandraClient
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.pgclient.PgPool
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named

@Context
class AppEventListener {

    companion object {
        private val log = LoggerFactory.getLogger(AppEventListener::class.java)
    }

    @Inject
    lateinit var vertx: Vertx

    @Inject  @field:Named("nats")
    lateinit var natsProjectionVerticle: EventsPublisherVerticle
    @Inject
    lateinit var natsProjectorVerticle: NatsProjectorVerticle

    @Inject  @field:Named("users")
    lateinit var usersProjectionVerticle: EventsPublisherVerticle
    @Inject
    lateinit var userProjectorVerticle: UserProjectorVerticle

    @Named("pgPool")
    lateinit var pgPool: PgPool

//    @Inject
//    lateinit var cassandra : CassandraClient
//    @Inject
//    lateinit var cassandraConfig : CassandraConfig

    @EventListener
    internal fun onStartupEvent(event: StartupEvent) {
        vertx.registerLocalCodec()
        val deploymentOptions = DeploymentOptions().setHa(false).setInstances(1)
//        val ddls = ddls()
//        cassandra
//            .execute(ddls[0])
//            .compose { cassandra.execute(ddls[1]) }
//            .onFailure { log.error("Creating tables", it) }
//            .onSuccess {
                log.info("Scylla tables successfully created")
                vertx.deployVerticle(usersProjectionVerticle, deploymentOptions)
                    .compose { vertx.deployVerticle(userProjectorVerticle, deploymentOptions) }
//                    .compose { vertx.deployVerticle(natsProjectionVerticle, deploymentOptions) }
//                    .compose { vertx.deployVerticle(natsProjectorVerticle, deploymentOptions) }
                    .onSuccess { log.info("Successfully started $it") }
                    .onFailure { log.error("When starting", it) }
//            }
    }

    @EventListener
    internal fun onShutdownEvent(event: ShutdownEvent) {
        vertx.close()
        pgPool.close()
//        cassandra.close()
    }

//    @Inject
//    fun ddls(): List<String> {
//        val c1 = """CREATE KEYSPACE IF NOT EXISTS ${cassandraConfig.keyspace} WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 }""".trim()
//        val c2 = """CREATE TABLE IF NOT EXISTS ${cassandraConfig.keyspace}.users_view (id UUID, name VARCHAR, email VARCHAR, password VARCHAR, is_active BOOLEAN, PRIMARY KEY (email));""".trim()
//        //   val c3 = """CREATE INDEX user_password ON ${cassandraConfig.keyspace}.users_view (email);""".trim()
//        return listOf(c1, c2)
//    }

}