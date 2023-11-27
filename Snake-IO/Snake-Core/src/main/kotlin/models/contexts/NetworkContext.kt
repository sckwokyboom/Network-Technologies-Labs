package models.contexts

import api.config.NetworkConfig
import models.states.StateHolder

class NetworkContext(
    val networkConfig: NetworkConfig,
    stateHolder: StateHolder
) : Context(stateHolder)