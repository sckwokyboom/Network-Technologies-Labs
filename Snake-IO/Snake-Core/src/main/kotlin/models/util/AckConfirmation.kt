package models.util

import dto.messages.Message

data class AckConfirmation(
    var messageSentTime : Long,
    val message: Message,
)
