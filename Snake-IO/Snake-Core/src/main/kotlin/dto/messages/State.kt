package dto.messages

import models.core.GameState
import java.net.InetSocketAddress

class State(
    address: InetSocketAddress,
    val state: GameState
) : Message(address)
