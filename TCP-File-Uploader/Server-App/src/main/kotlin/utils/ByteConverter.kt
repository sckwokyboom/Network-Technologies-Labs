package utils

object ByteConverter {
    fun convert(bytes: Long): String {
        val gigabyte = 1024 * 1024 * 1024
        val megabyte = 1024 * 1024
        val kilobyte = 1024

        return when {
            bytes >= gigabyte -> {
                val gb = bytes.toDouble() / gigabyte
                "%.2f [GB]".format(gb)
            }

            bytes >= megabyte -> {
                val mb = bytes.toDouble() / megabyte
                "%.2f [MB]".format(mb)
            }

            bytes >= kilobyte -> {
                val kb = bytes.toDouble() / kilobyte
                "%.2f [KB]".format(kb)
            }

            else -> "$bytes [B]"
        }
    }
}