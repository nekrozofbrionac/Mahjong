package de.nekrozofbrionac.mahjong.player.control.bot.ml

import de.nekrozofbrionac.mahjong.board.TileType

/*
* a tile should probably have values:
* does it exist? bool
* is a suited tile? bool
* is it bamboo?
* is it stone?
* is it character?
* numeral? hoe
* is it an honour tile? bool
* is it a wind? bool
* is it eastWind?
* is it southWind?
* is it westWind?
* is it northWind?
* is it a dragon? bool
* is it a greenD?
* is it a whiteD?
* is it a redD?
* is it a flower/seasonTile?
* is it a flower?
* is it flower1?
* is it flower2?
* is it flower3?
* is it flower4?
* is it a season?
* is it season1?
* is it season2?
* is it season3?
* is it season4?
* */
fun tileTypeToWeights (tile: TileType): FloatArray {
  return when(tile) {
    TileType.BAMBOO_1 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.BAMBOO_2 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.BAMBOO_3 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.BAMBOO_4 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.BAMBOO_5 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.BAMBOO_6 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.BAMBOO_7 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.BAMBOO_8 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.BAMBOO_9 ->     floatArrayOf(1.0f,   1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)

    TileType.STONE_1 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.STONE_2 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.STONE_3 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.STONE_4 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.STONE_5 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.STONE_6 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.STONE_7 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.STONE_8 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.STONE_9 ->      floatArrayOf(1.0f,   1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)

    TileType.CHARACTER_1 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.CHARACTER_2 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.CHARACTER_3 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.CHARACTER_4 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.CHARACTER_5 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.CHARACTER_6 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.CHARACTER_7 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.CHARACTER_8 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.CHARACTER_9 ->  floatArrayOf(1.0f,   1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)

    TileType.WIND_EAST    -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  1.0f, 1.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.WIND_SOUTH   -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  1.0f, 0.0f, 1.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.WIND_WEST    -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f, 1.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.WIND_NORTH   -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  1.0f, 0.0f, 0.0f, 0.0f, 1.0f,    0.0f, 0.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)

    TileType.DRAGON_GREEN -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    1.0f, 1.0f, 0.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.DRAGON_WHITE -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    1.0f, 0.0f, 1.0f, 0.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.DRAGON_RED   -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    1.0f, 0.0f, 0.0f, 1.0f,    0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)


    TileType.FLOWER_1     -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    1.0f,    1.0f, 1.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.FLOWER_2     -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    1.0f,    1.0f, 0.0f, 1.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.FLOWER_3     -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    1.0f,    1.0f, 0.0f, 0.0f, 1.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
    TileType.FLOWER_4     -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    1.0f,    1.0f, 0.0f, 0.0f, 0.0f, 1.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f)

    TileType.SEASON_1     -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    1.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    1.0f, 1.0f, 0.0f, 0.0f, 0.0f)
    TileType.SEASON_2     -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    1.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    1.0f, 0.0f, 1.0f, 0.0f, 0.0f)
    TileType.SEASON_3     -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    1.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    TileType.SEASON_4     -> floatArrayOf(1.0f,   0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,  0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    0.0f, 0.0f, 0.0f, 0.0f,    1.0f,    0.0f, 0.0f, 0.0f, 0.0f, 0.0f,    1.0f, 0.0f, 0.0f, 0.0f, 1.0f)
  }
}

