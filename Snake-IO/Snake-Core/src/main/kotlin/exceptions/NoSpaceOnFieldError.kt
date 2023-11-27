package exceptions

class NoSpaceOnFieldError(
    message: String? = null,
    cause: Throwable? = null,
) : BusinessError(message, cause)