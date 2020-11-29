package com.example.facebook_clone

import android.util.Log
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

private const val TAG = "Test"
fun main() = runBlocking {
        val one = GlobalScope.async { doSomethingUsefulOne() }
        println("The answer is ${one.await()}")
}

suspend fun doSomethingUsefulOne(): Int {
    delay(3000)
    return 159
}
