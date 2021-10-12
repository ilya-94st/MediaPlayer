package com.example.mediaplayer.other

open class Event<out T>(private var data: T) {
var hasBeenHandled = false
    private set

    fun getContentIfNotHandled(): T?{
        return if (hasBeenHandled) {
            null
        } else {
         hasBeenHandled = true
            data
        }
    }
    // функция нужна для получение данных , которые уже обработаны
    fun peekContent() = data
}