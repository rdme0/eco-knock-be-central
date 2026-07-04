package jnu.econovation.ecoknockbecentral.airquality.messaging.consumer

interface Consumer {
    fun start()
    fun cancel()
    suspend fun consume()
}