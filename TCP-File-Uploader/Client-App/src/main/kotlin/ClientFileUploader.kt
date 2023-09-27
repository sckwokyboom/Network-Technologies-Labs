import options.ClientOptionsParser
import utils.ByteConverter
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.Socket
import java.time.Duration
import java.time.Instant

class ClientFileUploader {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val configuration = ClientOptionsParser().parse(args)
                val file = configuration.filePath.toFile()
                validateFile(file)
                Socket(configuration.host, configuration.port).use { socket ->
                    FileInputStream(file).use { fileIn ->

//                    val info = UploadInfo(file.name, file.length())
//                    println("Sending file ${info.fileName} to the server.")
//
//                    socket.getOutputStream().use { out ->
//                        ObjectOutputStream(out).writeObject(info)
//                        out.flush()
//
//                        transferTo(fileIn, out)
//                        println("File ${info.fileName} sent to the server.")
//                    }
                        socket.getOutputStream().use { out ->
                            DataOutputStream(out).writeUTF(file.toPath().fileName.toString())
                            DataOutputStream(out).writeLong(file.length())
                            out.flush()
                            transferTo(fileIn, out)
                            println("File ${file.toPath().fileName} sent to the server.")
//                    }
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                System.err.println(e.message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        private fun validateFile(file: File) {
            if (!file.isFile) {
                throw IllegalArgumentException("The specified path does not contain a file.")
            }

            if (file.name.toByteArray(Charsets.UTF_8).size > 4096) {
                throw IllegalArgumentException("File name is greater than 4096 bytes.")
            }

            if (file.length() > 1e+12) {
                throw IllegalArgumentException("File size is greater than 1 terabyte.")
            }
        }

        @JvmStatic
        private fun transferTo(fileIn: FileInputStream, out: OutputStream) {
            val bufferSize = 4 * 1024
            val buffer = ByteArray(bufferSize)

            var totalSent = 0
            var startTime = Instant.now()
            var sentNow = 0

            while (true) {
                val bytesRead = fileIn.read(buffer)
                if (bytesRead == -1) {
                    break
                }

                out.write(buffer, 0, bytesRead)
                out.flush()

                totalSent += bytesRead
                sentNow += bytesRead

                val currentTime = Instant.now()
                val timeElapsed = Duration.between(startTime, currentTime).toMillis()

                if (timeElapsed >= 3000) {
                    println("${ByteConverter.convert(sentNow.toLong())}/SEC")
                    sentNow = 0
                    startTime = currentTime
                }
            }

            if (sentNow > 0) {
                println("${ByteConverter.convert(sentNow.toLong())}/SEC")
            }
        }
    }
}