package models.states

import dto.messages.Announcement
import models.core.Coord
import models.core.GameConfig
import models.core.GamePlayer
import models.core.Snake

interface ClientState {
    fun getFoods(): List<Coord>
    fun getSnakes(): List<Snake>

    fun getPlayers(): List<GamePlayer>

    fun getAnnouncements(): List<Announcement>

    fun getCurNodePlayer(): GamePlayer
    fun isGameRunning(): Boolean

    fun getConfig(): GameConfig

}