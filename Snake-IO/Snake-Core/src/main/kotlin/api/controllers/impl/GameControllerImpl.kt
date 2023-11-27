package api.controllers.impl

import api.controllers.GameController
import exceptions.NoGameError
import models.contexts.Context
import models.core.Direction
import models.requests.SteerRequest

class GameControllerImpl(
    context: Context
) : GameController {

    private val stateHolder = context.stateHolder


    override fun move(direction: Direction) {
        runCatching {
            stateHolder.getState().getMasterPlayer()
        }.onSuccess { master ->
            if (stateHolder.getState().getGameAddress() == master.ip) {
                stateHolder.getStateEditor().updateSnakeDirection(master.id, direction)
            } else {
                val steerRequest = SteerRequest(master.ip, direction)
                stateHolder.getStateEditor().setSteerRequest(steerRequest)
            }
        }.onFailure { e ->
            throw NoGameError("Error on move snake", e)
        }

    }
}