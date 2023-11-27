package exceptions

class UnknownPlayerError(
    message: String? = null,
    cause: Throwable? = null
) : BusinessError(message, cause)