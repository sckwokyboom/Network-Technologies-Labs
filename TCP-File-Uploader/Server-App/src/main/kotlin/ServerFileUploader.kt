import models.Task
import options.ServerOptionsParser
import java.io.DataInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

class ServerFileUploader {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val configuration = ServerOptionsParser().parse(args)
                println("Your configuration: port = ${configuration.port}; upload dir = ${configuration.uploadDir}")
                val uploadDir = Path(configuration.uploadDir)

                createUploadDirectory(uploadDir)

                startServer(configuration.port, uploadDir)
            } catch (e: IllegalArgumentException) {
                System.err.println(e.message)
            }
        }

        @JvmStatic
        private fun createUploadDirectory(uploadDir: Path) {
            try {
                Files.createDirectories(uploadDir)
                println("Upload directory created: $uploadDir.")
            } catch (e: IOException) {
                e.printStackTrace()
                println("Failed to create the upload directory: $uploadDir.")
            }
        }

        @JvmStatic
        private fun startServer(port: Int, uploadDir: Path) {
            try {
                ServerSocket(port).use { serverSocket ->
                    println("Server is waiting for connections on port $port...")
                    while (true) {
                        val clientSocket = serverSocket.accept()
                        println("Connected client: ${clientSocket.inetAddress}.")

                        val clientThread = ClientHandleTask(clientSocket, uploadDir)
                        clientThread.start()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                println("Server encountered an error: ${e.message}")
            }
        }
    }
}

private class ClientHandleTask(private val socket: Socket, private val uploadDir: Path) : Task() {

    override fun run() {
        try {
            DataInputStream(socket.getInputStream()).use { input ->
//                val obj: Any = ObjectInputStream(input).readObject()
//
//                val uploadInfo = if (obj is UploadInfo) {
//                    println(obj)
//                    obj
//                } else {
//                    println(
//                        "Unable to receive the file; transmission protocol violation."
//                    )
//                    return
//                }

                val filename = input.readUTF()
                val filesize = input.readLong()

                val path = uploadDir.resolve(filename)
                FileOutputStream(path.toFile()).use { fileOut ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        fileOut.write(buffer, 0, bytesRead)
                    }
                }
                println("File successfully received and saved to $path.")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            close()
        }
    }

    override fun close() {
        try {
            socket.close()
            println("${socket.port} was closed.")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}