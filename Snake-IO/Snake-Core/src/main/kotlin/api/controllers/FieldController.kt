package api.controllers

import models.contexts.Context
import models.core.*
import models.requests.GameCreateRequest
import models.requests.MoveSnakeTaskRequest
import models.states.State
import message_utils.IdSequence
import java.io.Closeable
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.random.Random

class FieldController(
    context: Context
) : Closeable {
    private data class FieldSize(
        val width: Int,
        val height: Int,
    )

    companion object {
        private const val SCHEDULED_PULL_SIZE = 4
        private const val INITIAL_DELAY = 0L
        private const val SCAN_FIELD_TASK_DELAY = 200L
        private const val CREATING_SNAKES_TASK_DELAY = 300L
        private const val SPAWN_FOOD_TASK_DELAY = 1000L
        private const val CREATE_GAME_TASK_DELAY = 300L
    }

    private val stateHolder = context.stateHolder

    private var fieldSize = FieldSize(0, 0)
    private var isNewGame = AtomicBoolean(false)

    private val schedulerExecutor = Executors.newScheduledThreadPool(SCHEDULED_PULL_SIZE)

    private var moveSnakeTaskFuture: Optional<ScheduledFuture<*>> = Optional.empty()


    private val scanFieldTask = {
        if (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()
            val config = state.getConfig()
            if (isNewGame.get()) {
                fieldSize = FieldSize(config.width, config.height)
            }

            state.getCurNodePlayer()

            val snakeCoords = state.getSnakes().stream().flatMap { snake -> snake.points.stream() }.toList()
            val foodCoords = state.getFoods()

            val availableCoords = findAvailableCoords(snakeCoords, foodCoords)

            stateHolder.getStateEditor().updateAvailableCoords(availableCoords)
        }
    }

    private val spawnFoodTask = {
        if (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()
            val config = state.getConfig()
            val foods = state.getFoods()

            if (foods.size != config.foodStatic) {
                val newFoods = mutableListOf<Coord>()

                for (i in 0..config.foodStatic - foods.size) {
                    var coord = Coord((0..config.width).random(), (0..config.height).random())
                    while (foods.contains(coord)) {
                        coord = Coord((0..config.width).random(), (0..config.height).random())
                    }
                    newFoods.add(coord)
                }

                stateHolder.getStateEditor().addFoods(newFoods)
            }
        }
    }

    private val moveSnakesTask = {
        if (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()
            val newCoords = mutableListOf<Coord>()
            val snakes = state.getSnakes().toMutableList()
            val foods = state.getFoods().toMutableList()
            val players = state.getPlayers().toMutableList()

            for (snake in snakes) {
                val direction = snake.headDirection
                val curCoord = snake.points[0]

                val newCoord: Coord

                when (direction) {
                    Direction.UP -> {
                        newCoord = if (curCoord.y == 0) {
                            Coord(curCoord.x, fieldSize.height - 1)
                        } else {
                            Coord(curCoord.x, curCoord.y - 1)
                        }

                    }

                    Direction.DOWN -> {
                        newCoord = if (curCoord.y == fieldSize.height - 1) {
                            Coord(curCoord.x, 0)
                        } else {
                            Coord(curCoord.x, curCoord.y + 1)
                        }
                    }

                    Direction.LEFT -> {
                        newCoord = if (curCoord.x == 0) {
                            Coord(fieldSize.width - 1, curCoord.y)
                        } else {
                            Coord(curCoord.x - 1, curCoord.y)
                        }
                    }

                    Direction.RIGHT -> {
                        newCoord = if (curCoord.x == fieldSize.width - 1) {
                            Coord(0, curCoord.y)
                        } else {
                            Coord(curCoord.x + 1, curCoord.y)
                        }
                    }
                }

                newCoords.add(newCoord)
            }

            val snakesToDelete = mutableListOf<Snake>()
            val coordsToDelete = mutableListOf<Coord>()
            val deadPlayers = mutableListOf<GamePlayer>()


            for (i in 0 until newCoords.size) {
                val coord = newCoords[i]

                if (coord in snakes[i].points) {
                    snakesToDelete.add(snakes[i])
                    coordsToDelete.add(coord)
                    val player = players.stream().filter { p -> p.id == snakes[i].playerId }.findFirst().get()
                    players[i] = player.copy(role = NodeRole.VIEWER, score = 0)
                    deadPlayers.add(players[i])
                }

                for (j in i + 1 until newCoords.size) {
                    if (coord == newCoords[j]) {
                        val otherSnake = snakes[j]
                        if (otherSnake !in snakesToDelete)
                            snakesToDelete.add(otherSnake)

                        coordsToDelete.add(coord)
                        val player = players.stream().filter { p -> p.id == otherSnake.playerId }.findFirst().get()
                        players[j] = player.copy(role = NodeRole.VIEWER, score = 0)
                        deadPlayers.add(players[j])
                    }
                }
            }

            players.removeAll(deadPlayers)
            snakes.removeAll(snakesToDelete)
            newCoords.removeAll(coordsToDelete)

            for (i in 0 until newCoords.size) {
                val snake = snakes[i]
                val coord = newCoords[i]
                val newSnakePoints = mutableListOf<Coord>()

                newSnakePoints.add(coord)
                newSnakePoints.addAll(snake.points)

                if (coord in foods) {
                    foods.remove(coord)
                    val player = players.stream().filter { p -> p.id == snake.playerId }.findFirst().get()
                    players[i] = player.copy(score = player.score + 1)

                } else {
                    newSnakePoints.removeLast()
                }

                snakes[i] = snake.copy(points = newSnakePoints)
            }

            stateHolder.getStateEditor().setFoods(foods)
            stateHolder.getStateEditor().setSnakes(snakes)
            stateHolder.getStateEditor().updatePlayers(players)
            stateHolder.getStateEditor().updatePlayers(deadPlayers)

        }
    }

    private val createSnakesTask = {
        if (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()
            val playersToAdding = state.getPlayersToAdding()
            val availableCoords = state.getAvailableCoords().toMutableList()
            for (player in playersToAdding) {
                if (availableCoords.isEmpty()) {
                    break
                }
                val headCoord = availableCoords.random()
                val bodyCoord = getRandomSecondSnakeCoord(headCoord)
                val snake = Snake(
                    player.id,
                    listOf(headCoord, bodyCoord),
                    SnakeState.ALIVE,
                    getHeadDirection(headCoord, bodyCoord)
                )

                availableCoords.remove(headCoord)
                stateHolder.getStateEditor().removePlayerToAdding(player)
                stateHolder.getStateEditor().addPlayer(player)
                stateHolder.getStateEditor().updateAvailableCoords(availableCoords)
                stateHolder.getStateEditor().addSnake(snake)
            }
        }
    }

    private val checkGameRequestTask = {
        val state = stateHolder.getState()
        val gameCreateRequestOpt = state.getGameCreateRequest()
        if (gameCreateRequestOpt.isPresent) {
            createGame(state, gameCreateRequestOpt.get())
        }
        if (state.getMoveSnakeTaskRequest() == MoveSnakeTaskRequest.RUN) {
            if (moveSnakeTaskFuture.isEmpty) {
                moveSnakeTaskFuture = Optional.of(
                    schedulerExecutor.scheduleWithFixedDelay(
                        moveSnakesTask,
                        INITIAL_DELAY,
                        state.getConfig().stateDelayMs.toLong(),
                        TimeUnit.MILLISECONDS
                    )
                )
            }
        } else if (state.getMoveSnakeTaskRequest() == MoveSnakeTaskRequest.STOP) {
            if (moveSnakeTaskFuture.isPresent) {
                moveSnakeTaskFuture.get().cancel(true)
                moveSnakeTaskFuture = Optional.empty()
            }
        }

    }

    init {
        schedulerExecutor.scheduleWithFixedDelay(
            scanFieldTask,
            INITIAL_DELAY,
            SCAN_FIELD_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )
        schedulerExecutor.scheduleWithFixedDelay(
            createSnakesTask,
            INITIAL_DELAY,
            CREATING_SNAKES_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )
        schedulerExecutor.scheduleWithFixedDelay(
            spawnFoodTask,
            INITIAL_DELAY,
            SPAWN_FOOD_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )
        schedulerExecutor.scheduleWithFixedDelay(
            checkGameRequestTask,
            INITIAL_DELAY,
            CREATE_GAME_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )
    }

    private fun createGame(state: State, gameCreateRequest: GameCreateRequest) {
        stateHolder.getStateEditor().setGameConfig(gameCreateRequest.gameConfig)
        stateHolder.getStateEditor().setGameName(gameCreateRequest.gameName)

        val player = GamePlayer(
            name = stateHolder.getState().getPlayerName(),
            id = IdSequence.getNextId(),
            role = NodeRole.MASTER,
        )

        fieldSize = FieldSize(gameCreateRequest.gameConfig.width, gameCreateRequest.gameConfig.height)

        val availableCoords = findAvailableCoords(emptyList(), state.getFoods())
        stateHolder.getStateEditor().setNodeRole(NodeRole.MASTER)
        stateHolder.getStateEditor().updateAvailableCoords(availableCoords)
        stateHolder.getStateEditor().addPlayerToAdding(player)
        stateHolder.getStateEditor().setNodeId(player.id)
        stateHolder.getStateEditor().setGameAddress(player.ip)
        stateHolder.getStateEditor().setCurNodePlayer(player)
        stateHolder.getStateEditor().clearGameCreateRequest()
    }

    private fun findAvailableCoords(snakesCoords: List<Coord>, foodCoords: List<Coord>): List<Coord> {
        val allCoords = (0 until fieldSize.width).flatMap { x ->
            (0 until fieldSize.height).map { y -> Coord(x, y) }
        }.toMutableList()

        for (snakeCoord in snakesCoords) {
            allCoords.removeAll { coord ->
                (abs(coord.x - snakeCoord.x) <= 2 && abs(coord.y - snakeCoord.y) <= 2)
            }
        }

        allCoords.removeAll { coord -> foodCoords.contains(coord) }

        return allCoords
    }

    private fun getRandomSecondSnakeCoord(headCoord: Coord): Coord {
        return when (Random.nextInt(0, 4)) {
            0 -> Coord(if (headCoord.x != 0) headCoord.x - 1 else fieldSize.width - 1, headCoord.y)
            1 -> Coord(if (headCoord.x != fieldSize.width - 1) headCoord.x + 1 else 0, headCoord.y)
            2 -> Coord(headCoord.x, if (headCoord.y != fieldSize.height - 1) headCoord.y + 1 else 0)
            3 -> Coord(headCoord.x, if (headCoord.y != 0) headCoord.y - 1 else fieldSize.height - 1)
            else -> Coord(-1, -1)
        }
    }

    private fun getHeadDirection(headCoord: Coord, bodyCoord: Coord): Direction {
        return if (bodyCoord.x == 0 && headCoord.x == fieldSize.width - 1) {
            Direction.LEFT
        } else if (bodyCoord.x == fieldSize.width - 1 && headCoord.x == 0) {
            Direction.RIGHT
        } else if (bodyCoord.y == 0 && headCoord.y == fieldSize.height - 1) {
            Direction.UP
        } else if (bodyCoord.y == fieldSize.height - 1 && headCoord.y == 0) {
            Direction.DOWN
        } else if (bodyCoord.x > headCoord.x) {
            Direction.LEFT
        } else if (bodyCoord.x < headCoord.x) {
            Direction.RIGHT
        } else if (bodyCoord.y > headCoord.y) {
            Direction.UP
        } else {
            Direction.DOWN
        }
    }

    override fun close() {
        schedulerExecutor.shutdown()
    }
}