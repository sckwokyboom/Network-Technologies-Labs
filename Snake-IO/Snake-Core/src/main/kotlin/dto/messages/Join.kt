package dto.messages

import models.core.NodeRole
import models.core.PlayerType
import java.net.InetSocketAddress

class Join(
    address: InetSocketAddress,
    val playerType: PlayerType = PlayerType.HUMAN,
    val playerName: String,
    val gameName: String,
    val requestedRole: NodeRole
) : Message(address)
