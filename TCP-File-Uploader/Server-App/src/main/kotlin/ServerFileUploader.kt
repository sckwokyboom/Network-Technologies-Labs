import exception.ProtocolViolationException
import models.Task
import options.ServerOptionsParser
import utils.ByteConverter
import java.io.DataInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.io.path.Path

object ServerFileUploader {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val configuration = ServerOptionsParser.parse(args)
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
            println("Upload directory successfully created: $uploadDir.")
        } catch (e: IOException) {
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

                    val clientThread = ClientTask(clientSocket, uploadDir)
                    clientThread.start()
                }
            }
        } catch (e: IOException) {
            System.err.println("Server encountered an error: ${e.message}")
        }
    }
}

private class ClientTask(private val socket: Socket, private val uploadDir: Path) : Task() {

    override fun run() {
        try {
            DataInputStream(socket.getInputStream()).use { input ->
                val fileName = input.readUTF()
                val fileSize = input.readLong()

                val path = uploadDir.resolve(fileName)
                FileOutputStream(path.toFile()).use { fileOut ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int
                    var totalRead = 0
                    var receivedNow = 0
                    var startTime = Instant.now()
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        fileOut.write(buffer, 0, bytesRead)
                        receivedNow += bytesRead
                        totalRead += bytesRead
                        val currentTime = Instant.now()
                        val timeElapsed = Duration.between(startTime, currentTime).toMillis()
                        if (timeElapsed >= 3000) {
                            println("${ByteConverter.convertToHumanReadable(receivedNow.toLong())}/s")
                            receivedNow = 0
                            startTime = currentTime
                        }
                    }
                    if (receivedNow > 0) {
                        println("${ByteConverter.convertToHumanReadable(receivedNow.toLong())}/s")
                    }
                    if (fileSize != totalRead.toLong()) {
                        throw ProtocolViolationException("The specified file size does not match the size of the received file. Expected size: $fileSize [B], but was: $totalRead [B].")
                    }
                }
                println("File \"$fileName\" ($fileSize [B]) successfully received and saved to $path.")
            }
        } catch (e: IOException) {
            System.err.println("Protocol violation: Unable to receive the file.")
        } catch (e: ProtocolViolationException) {
            System.err.println("Protocol violation: ${e.message}")
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