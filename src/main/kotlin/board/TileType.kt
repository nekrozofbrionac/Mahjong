package de.nekrozofbrionac.mahjong.board;

enum class TileType(
  val count: Int,
  val shortHand: String
) {
  BAMBOO_1(4, "B1"),
  BAMBOO_2(4, "B2"),
  BAMBOO_3(4, "B3"),
  BAMBOO_4(4, "B4"),
  BAMBOO_5(4, "B5"),
  BAMBOO_6(4, "B6"),
  BAMBOO_7(4, "B7"),
  BAMBOO_8(4, "B8"),
  BAMBOO_9(4, "B9"),
  STONE_1(4, "S1"),
  STONE_2(4, "S2"),
  STONE_3(4, "S3"),
  STONE_4(4, "S4"),
  STONE_5(4, "S5"),
  STONE_6(4, "S6"),
  STONE_7(4, "S7"),
  STONE_8(4, "S8"),
  STONE_9(4, "S9"),
  CHARACTER_1(4, "C1"),
  CHARACTER_2(4, "C2"),
  CHARACTER_3(4, "C3"),
  CHARACTER_4(4, "C4"),
  CHARACTER_5(4, "C5"),
  CHARACTER_6(4, "C6"),
  CHARACTER_7(4, "C7"),
  CHARACTER_8(4, "C8"),
  CHARACTER_9(4, "C9"),
  WIND_EAST(4, "WE"),
  WIND_SOUTH(4, "WS"),
  WIND_WEST(4, "WW"),
  WIND_NORTH(4, "WN"),
  DRAGON_GREEN(4, "DG"),
  DRAGON_WHITE(4, "DR"),
  DRAGON_RED(4, "DR"),
  FLOWER_1(1, "F1"),
  FLOWER_2(1, "F2"),
  FLOWER_3(1, "F3"),
  FLOWER_4(1, "F4"),
  SEASON_1(1, "S1"),
  SEASON_2(1, "S2"),
  SEASON_3(1, "S3"),
  SEASON_4(1, "S4"),

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