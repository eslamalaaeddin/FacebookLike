package com.example.facebook_clone

fun main(){
    val map = hashMapOf<String, Any>("islam" to "Islam AlaaEddin")
    println(map)
    val key: String = map.keys.toList()[0]
    val value: String = map.values.toList()[0] as String

    println(key)
    println(value)

}