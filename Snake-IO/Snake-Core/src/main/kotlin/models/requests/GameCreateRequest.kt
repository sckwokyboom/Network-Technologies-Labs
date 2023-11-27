package models.requests

import models.core.GameConfig


data class GameCreateRequest(
    val gameName: String,
    val gameConfig: GameConfig
)