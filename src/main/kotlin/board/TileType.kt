package de.nekrozofbrionac.mahjong.board;

enum class TileType(
  val count: Int,
  val shortHand: String,
  val numeral: Int?,
  val suit: String?,
) {
  BAMBOO_1(4, "B1", 1, "B"),
  BAMBOO_2(4, "B2", 2, "B"),
  BAMBOO_3(4, "B3", 3, "B"),
  BAMBOO_4(4, "B4", 4, "B"),
  BAMBOO_5(4, "B5", 5, "B"),
  BAMBOO_6(4, "B6", 6, "B"),
  BAMBOO_7(4, "B7", 7, "B"),
  BAMBOO_8(4, "B8", 8, "B"),
  BAMBOO_9(4, "B9", 9, "B"),
  STONE_1(4, "S1", 1, "S"),
  STONE_2(4, "S2", 2, "S"),
  STONE_3(4, "S3", 3, "S"),
  STONE_4(4, "S4", 4, "S"),
  STONE_5(4, "S5", 5, "S"),
  STONE_6(4, "S6", 6, "S"),
  STONE_7(4, "S7", 7, "S"),
  STONE_8(4, "S8", 8, "S"),
  STONE_9(4, "S9", 9, "S"),
  CHARACTER_1(4, "C1", 1, "C"),
  CHARACTER_2(4, "C2", 2, "C"),
  CHARACTER_3(4, "C3", 3, "C"),
  CHARACTER_4(4, "C4", 4, "C"),
  CHARACTER_5(4, "C5", 5, "C"),
  CHARACTER_6(4, "C6", 6, "C"),
  CHARACTER_7(4, "C7", 7, "C"),
  CHARACTER_8(4, "C8", 8, "C"),
  CHARACTER_9(4, "C9", 9, "C"),
  WIND_EAST(4, "WE", null, null),
  WIND_SOUTH(4, "WS", null, null),
  WIND_WEST(4, "WW", null, null),
  WIND_NORTH(4, "WN", null, null),
  DRAGON_GREEN(4, "DG", null, null),
  DRAGON_WHITE(4, "DW", null, null),
  DRAGON_RED(4, "DR", null, null),
  FLOWER_1(1, "F1", null, null),
  FLOWER_2(1, "F2", null, null),
  FLOWER_3(1, "F3", null, null),
  FLOWER_4(1, "F4", null, null),
  SEASON_1(1, "S1", null, null),
  SEASON_2(1, "S2", null, null),
  SEASON_3(1, "S3", null, null),
  SEASON_4(1, "S4", null, null),

  ;

  fun isFlower(): Boolean {
    return when(this) {
      FLOWER_1 -> true;
      FLOWER_2 -> true;
      FLOWER_3 -> true;
      FLOWER_4 -> true;
      SEASON_1 -> true;
      SEASON_2 -> true;
      SEASON_3 -> true;
      SEASON_4 -> true;
      else -> false
    }
  }
}