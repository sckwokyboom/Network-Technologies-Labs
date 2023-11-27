package models.states

import models.core.GameAnnouncement
import models.core.GameState

interface StateHolder {
    fun isNodeMaster(): Boolean


    fun getGameAnnouncement(): GameAnnouncement


    fun getState(): State
    fun getStateEditor(): StateEditor

    fun getGameState(): GameState
}

