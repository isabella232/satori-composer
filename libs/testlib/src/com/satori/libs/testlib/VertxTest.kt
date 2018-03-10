package com.satori.libs.testlib

import io.vertx.core.*
import io.vertx.ext.unit.junit.*
import org.junit.*
import java.util.concurrent.*

open class VertxTest : Assert() {
  
  @Rule
  @JvmField
  val rule = RunTestOnContext {
    val vertx = Vertx.vertx(VertxOptions().apply {
      eventLoopPoolSize = 1
    })
    vertx.exceptionHandler { ex ->
      println("error: $ex")
    }
  }
  
  fun vertx() = rule.vertx()
  
  fun timestamp() = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}
