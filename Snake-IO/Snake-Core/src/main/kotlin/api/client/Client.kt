package api.client

import api.MessageManager
import api.config.NetworkConfig
import api.controllers.FieldController
import models.contexts.NetworkContext
import models.states.ClientState
import models.states.impl.StateHolderImpl
import java.io.Closeable

class Client : Closeable {
    private val stateHolder = StateHolderImpl()
    private val context = NetworkContext(NetworkConfig(), stateHolder)

    private val fieldController = FieldController(context)
    private val messageManager = MessageManager(context)

    fun getState(): ClientState = context.stateHolder.getState()

    override fun close() {
        messageManager.close()
        fieldController.close()
    }
}