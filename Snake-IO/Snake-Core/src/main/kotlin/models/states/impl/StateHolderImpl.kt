package models.states.impl

import models.core.GameAnnouncement
import models.core.GameState
import models.core.NodeRole
import models.states.State
import models.states.StateEditor
import models.states.StateHolder

class StateHolderImpl : StateHolder {
    private val stateEditor = StateEditorImpl()
    private var cachedState: State

    init {
        cachedState = getState()
    }

    override fun isNodeMaster(): Boolean {
        return cachedState.getNodeRole() == NodeRole.MASTER
    }

    override fun getGameAnnouncement(): GameAnnouncement {

        return GameAnnouncement(
            cachedState.getPlayers(),
            cachedState.getConfig(),
            cachedState.getAvailableCoords().isNotEmpty(),
            cachedState.getGameName()
        )
    }

    override fun getState(): State {
        cachedState = stateEditor.edit()
        return cachedState
    }

    override fun getStateEditor(): StateEditor = stateEditor
    override fun getGameState(): GameState {
        return GameState(
            cachedState.getStateOrder(),
            cachedState.getSnakes(),
            cachedState.getFoods(),
            cachedState.getPlayers(),
        )
    }

    internal fun setOnStateEditListener(onStateEdit: (State) -> Unit) {
        this.stateEditor.setOnStateEditListener(onStateEdit)
    }
}