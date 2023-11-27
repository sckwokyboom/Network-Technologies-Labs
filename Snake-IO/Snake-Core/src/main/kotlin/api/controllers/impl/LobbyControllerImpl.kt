package api.controllers.impl

import api.controllers.LobbyController
import models.contexts.Context
import models.core.GameConfig
import models.core.NodeRole
import models.requests.ChangeRoleRequest
import models.requests.GameCreateRequest
import models.requests.JoinRequest
import java.net.InetSocketAddress

class LobbyControllerImpl(
    private val context: Context
) : LobbyController {

    override fun join(address: InetSocketAddress, gameName: String) {
        val joinRequest = JoinRequest(address, NodeRole.NORMAL)
        context.stateHolder.getStateEditor().setJoinRequest(joinRequest)
    }

    override fun watch(address: InetSocketAddress, gameName: String) {
        val joinRequest = JoinRequest(address, NodeRole.VIEWER)
        context.stateHolder.getStateEditor().setJoinRequest(joinRequest)
    }

    override fun leave() {
        val state = context.stateHolder.getState()

        runCatching {
            val receiverPlayer = state.getMasterPlayer()

            runCatching {
                val senderPlayer = state.getCurNodePlayer()

                val leaveRequest = ChangeRoleRequest(
                    senderPlayer.id,
                    receiverPlayer.id,
                    senderPlayer.role,
                    receiverPlayer.role
                )

                context.stateHolder.getStateEditor().setLeaveRequest(leaveRequest)
            }.onFailure { e ->
                throw e
            }

        }.onFailure { e ->
            e.message?.let { context.stateHolder.getStateEditor().addError(it) }
        }
    }

    override fun createGame(
        playerName: String,
        gameName: String,
        width: Int,
        height: Int,
        foodStatic: Int,
        stateDelay: Int
    ) {
        val gameConfig = GameConfig(width, height, foodStatic, stateDelay)
        val gameCreateRequest = GameCreateRequest(gameName, gameConfig)

        context.stateHolder.getStateEditor().setPlayerName(playerName)
        context.stateHolder.getStateEditor().setGameCreateRequest(gameCreateRequest)
    }
}