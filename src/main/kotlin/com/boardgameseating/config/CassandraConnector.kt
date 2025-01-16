package com.boardgameseating.config

import com.datastax.oss.driver.api.core.CqlSession
import java.net.InetSocketAddress

object CassandraConnector {
    private var session: CqlSession? = null

    fun connect(
        node: String = "127.0.0.1",
        port: Int = 9042,
        datacenter: String = "datacenter1",
        keyspaceName: String = "boardgames"
    ) {
        session = CqlSession.builder()
            .addContactPoint(InetSocketAddress(node, port))
            .withLocalDatacenter(datacenter)
            .withKeyspace(keyspaceName)
            .build()
    }

    fun getSession(): CqlSession {
        if (session == null) {
            connect()
        }
        return session!!
    }

    fun close() {
        session?.close()
    }
}