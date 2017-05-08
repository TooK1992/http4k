package org.http4k.http.contract

import org.http4k.http.core.HttpHandler
import org.http4k.http.core.Request
import org.http4k.http.core.Response
import org.http4k.http.core.Status.Companion.BAD_REQUEST
import org.http4k.http.core.Status.Companion.NOT_FOUND
import org.http4k.http.lens.LensFailure

typealias Router = (Request) -> HttpHandler?

interface Module {
    infix fun then(that: Module): Module {
        val thisBinding = toRouter()
        val thatBinding = that.toRouter()

        return object : Module {
            override fun toRouter(): Router = { req -> thisBinding(req) ?: thatBinding(req) }
        }
    }

    fun toHttpHandler(): HttpHandler {
        val handlerMatcher = toRouter()
        return { req ->
            handlerMatcher(req)?.let {
                try {
                    it(req)
                } catch (e: LensFailure) {
                    Response(BAD_REQUEST)
                }
            } ?: Response(NOT_FOUND)
        }
    }

    fun toRouter(): Router
}