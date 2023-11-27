package api.controllers

import java.net.InetSocketAddress

interface LobbyController : Controller {
    fun join(address: InetSocketAddress, gameName: String)
    fun watch(address: InetSocketAddress, gameName: String)
    fun leave()
    fun createGame(playerName: String, gameName: String, width: Int, height: Int, foodStatic: Int, stateDelay: Int)
}