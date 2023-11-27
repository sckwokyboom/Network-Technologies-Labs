package api.controllers

import api.config.NetworkConfig
import dto.messages.Ack
import dto.messages.Error
import dto.messages.Message
import message_utils.mappers.ProtoMapper
import me.ippolitov.fit.snakes.SnakesProto.GameMessage
import java.io.Closeable
import java.net.DatagramPacket
import java.net.InetSocketAddress
import java.net.MulticastSocket


class ReceiverController(
    config: NetworkConfig
) : Closeable {
    companion object {
        private const val BUFFER_SIZE = 1024
    }

    private val protoMapper = ProtoMapper
    private val buffer = ByteArray(BUFFER_SIZE)

    private val waitingForAck = mutableMapOf<InetSocketAddress, Long>()
    private val receivedAck = mutableMapOf<InetSocketAddress, Ack>()
    private val receivedErrors = mutableMapOf<InetSocketAddress, Error>()

    private var socket: MulticastSocket = initSocket(config)
    fun receive(): Message {
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        socket.receive(datagramPacket)
        val protoBytes = datagramPacket.data.copyOf(datagramPacket.length)
        val protoMessage = GameMessage.parseFrom(protoBytes)
        val address = InetSocketAddress(datagramPacket.address, datagramPacket.port)


        val message = protoMapper.toMessage(
            protoMessage,
            address
        )

        checkOnAck(message, protoMessage.msgSeq, address)
        checkOnError(message, protoMessage.msgSeq, address)

        return message
    }

    private fun checkOnAck(message: Message, msgSeq: Long, address: InetSocketAddress) {
        if (message is Ack) {
            synchronized(waitingForAck) {
                if (waitingForAck.containsKey(address) && waitingForAck[address] == msgSeq) {
                    waitingForAck.remove(address)
                    synchronized(receivedAck) {
                        receivedAck[address] = message
                    }
                }
            }
        }
    }

    private fun checkOnError(message: Message, msgSeq: Long, address: InetSocketAddress) {
        if (message is Error) {
            synchronized(waitingForAck) {
                if (waitingForAck.containsKey(address) && waitingForAck[address] == msgSeq) {
                    waitingForAck.remove(address)
                    synchronized(receivedErrors) {
                        receivedErrors[address] = message as Error
                    }
                }
            }
        }
    }

    fun addNodeForWaitingAck(address: InetSocketAddress, msqSeq: Long) {
        synchronized(waitingForAck) {
            waitingForAck.put(address, msqSeq)
        }
    }

    fun isAckInWaitingList(address: InetSocketAddress): Boolean {
        synchronized(waitingForAck) {
            return waitingForAck.containsKey(address)
        }
    }

    fun getReceivedAckByAddress(address: InetSocketAddress): Ack {
        synchronized(receivedAck) {
            val ack = receivedAck[address]
            receivedAck.remove(address)
            return ack ?: throw NoSuchElementException("Ack with this address has not in received Ack")
        }
    }

    fun getReceivedErrorByAddress(address: InetSocketAddress): Error {
        synchronized(receivedErrors) {
            val error = receivedErrors[address]
            receivedErrors.remove(address)
            return error ?: throw NoSuchElementException("Error message with this address has not in received Errors")
        }
    }

    private fun initSocket(config: NetworkConfig): MulticastSocket {
        val socket = MulticastSocket(config.groupAddress.port)
        socket.joinGroup(
            config.groupAddress,
            config.localInterface
        )
        return socket
    }

    override fun close() {
        socket.close()
    }
}