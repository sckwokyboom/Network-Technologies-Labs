package options

import java.nio.file.Path

data class ClientOptions(val filePath: Path, val host: String, val port: Int = 0) {
}