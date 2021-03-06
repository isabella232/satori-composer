package com.satori.libs.gradle.transform

import groovy.lang.*
import groovy.text.*
import org.codehaus.groovy.runtime.*
import org.gradle.api.*
import java.io.*
import java.util.*

open class TemplateScope(val engine: GStringTemplateEngine, val project: Project, val writer: Writer) : GroovyObjectSupport() {
  val props = HashMap<String, Any?>()
  
  override fun invokeMethod(name: String, args: Any?): Any? {
    when (name) {
      "include" -> {
        val arg = (args!! as Array<*>)[0]
        val file = when (arg) {
          is File -> arg
          is String -> project.file(arg)
          else -> {
            return super.invokeMethod(name, args)
          }
        }
        Transform.execute(this, file, engine, writer)
        return null// writable.toString()
      }
      else -> {
        return InvokerHelper.invokeMethod(project, name, args);
      }
    }
  }
  
  override fun setProperty(key: String, value: Any?) {
    props.put(key, value)
  }
  
  override fun getProperty(key: String): Any? {
    return props.get(key) ?: InvokerHelper.getProperty(project, key)
  }
}
