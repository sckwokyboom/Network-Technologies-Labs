package models.states

import dto.messages.Announcement
import models.core.*
import models.requests.*
import java.net.InetSocketAddress
import java.util.*


interface State : ClientState {
    override fun getFoods(): List<Coord>
    override fun getSnakes(): List<Snake>
    override fun getPlayers(): List<GamePlayer>
    override fun getAnnouncements(): List<Announcement>

    override fun getCurNodePlayer(): GamePlayer

    override fun isGameRunning(): Boolean

    override fun getConfig(): GameConfig

    fun getNodeRole(): NodeRole
    fun getPlayersToAdding(): List<GamePlayer>
    fun getDeputyListeners(): List<InetSocketAddress>


    fun getStateOrder(): Int
    fun getErrors(): Queue<String>


    fun getMasterPlayer(): GamePlayer


    fun getGameName(): String


    fun getGameAddress(): InetSocketAddress
    fun getPlayerName(): String
    fun getAvailableCoords(): List<Coord>
    fun getJoinRequest(): Optional<JoinRequest>
    fun getSteerRequest(): Optional<SteerRequest>
    fun getLeaveRequest(): Optional<ChangeRoleRequest>
    fun getGameCreateRequest(): Optional<GameCreateRequest>

    fun getDeputyListenTaskRequest(): DeputyListenTaskRequest

    fun getMoveSnakeTaskRequest(): MoveSnakeTaskRequest
}
