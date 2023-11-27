package api.controllers

import exceptions.NoGameError
import models.core.Direction

interface GameController : Controller {

    fun move(direction: Direction)
}