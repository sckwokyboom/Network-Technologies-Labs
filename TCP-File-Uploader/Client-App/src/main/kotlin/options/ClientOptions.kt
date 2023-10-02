package options

import java.nio.file.Path

data class ClientOptions(val fileToUploadPath: Path, val host: String, val port: Int) {
}