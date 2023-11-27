package dto.messages

import models.core.Direction
import java.net.InetSocketAddress


class Steer(
    address: InetSocketAddress,
    val direction: Direction
) : Message(address)
