package com.example.mediaplayer.other

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object{
        fun <T> successes(data: T?) = Resource(Status.SUCCESES, data , null)

        fun <T> error(message: String, data: T?) = Resource(Status.ERROR, data, message)

        fun <T> landing(data: T?) = Resource(Status.LOADING, data, null)
    }

    enum class Status{
     ERROR,
        SUCCESES,
        LOADING
    }
}