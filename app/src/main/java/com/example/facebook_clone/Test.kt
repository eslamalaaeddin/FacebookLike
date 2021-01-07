package com.example.facebook_clone

import android.util.Log
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

private const val TAG = "Test"
fun main() = runBlocking {
        val one = GlobalScope.async { doSomethingUsefulOne(1) }
        println("The answer is ${one.await()}")
}

suspend fun doSomethingUsefulOne(n: Int): Int {
    delay(3000)
    return n
}
