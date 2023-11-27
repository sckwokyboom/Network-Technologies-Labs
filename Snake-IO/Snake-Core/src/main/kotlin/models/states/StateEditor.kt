package models.states

import dto.messages.Announcement
import models.core.*
import models.requests.ChangeRoleRequest
import models.requests.GameCreateRequest
import models.requests.JoinRequest
import models.requests.SteerRequest
import java.net.InetSocketAddress


interface StateEditor {

    fun addFoods(foods: List<Coord>)

    fun setFoods(foods: List<Coord>)

    fun addPlayerToAdding(player: GamePlayer)

    fun removePlayerToAdding(player: GamePlayer)

    fun addPlayer(player: GamePlayer)

    fun updatePlayers(players: List<GamePlayer>)

    fun removePlayer(player: GamePlayer): Boolean

    fun addDeputyListeners(listeners: List<InetSocketAddress>)

    fun removeDeputyListener(listener: InetSocketAddress): Boolean

    fun addAnnouncement(announcement: Announcement)

    fun removeAnnouncement(announcement: Announcement): Boolean

    fun addSnake(snake: Snake)

    fun setSnakes(snakes: List<Snake>)

    fun removeSnake(snake: Snake): Boolean

    fun setNodeRole(nodeRole: NodeRole)

    fun setCurNodePlayer(player: GamePlayer)

    fun setStateOrder(stateOrder: Int)

    fun setGameName(name: String)
    fun setGameAddress(address: InetSocketAddress)

    fun setPlayerName(name: String)

    fun setGameConfig(gameConfig: GameConfig)

    fun setNodeId(id: Int)

    fun addError(errorMessage: String)

    fun updateAvailableCoords(coords: List<Coord>)


    fun updateRole(playerAddress: InetSocketAddress, senderRole: NodeRole, receiverRole: NodeRole)

    fun setState(newState: GameState)


    fun updateSnakeDirection(playerId: Int, direction: Direction)

    fun setJoinRequest(joinRequest: JoinRequest)
    fun clearJoinRequest()

    fun setSteerRequest(steerRequest: SteerRequest)
    fun clearSteerRequest()

    fun setLeaveRequest(leaveRequest: ChangeRoleRequest)
    fun clearLeaveRequest()

    fun setGameCreateRequest(gameCreateRequest: GameCreateRequest)
    fun clearGameCreateRequest()

    fun clearDeputyListenTaskToRun()

}