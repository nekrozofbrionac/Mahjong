import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.mahjong.player.control.PlayerControl

interface Player {
  val playerControl: PlayerControl

  val hand: List<TileType>;
  val melds: List<List<TileType>>
  val flowers: List<TileType>;

  fun sortHand()

  fun addToHand(tile: TileType);
  fun discard(tile: TileType): TileType?;

  fun formKongMeld(tile: TileType);
  fun formPongMeld(tile: TileType);
  // Forms a Chow indicated by its first tile.
  fun formChowMeld(tile: TileType);

  fun isKongAvailableFromDraw(tile: TileType): Boolean {
    return hand.count { it == tile } >= 4;
  }

  fun isKongAvailableInHand(tile: TileType): Boolean {
    return hand.count { it == tile } >= 3;
  }

  fun isPongAvailable(tile: TileType): Boolean {
    return hand.count { it == tile } >= 2;
  }

  fun isChowAvailable(tile: TileType): Boolean {
    if (tile.numeral == null || tile.suit == null) {
      return false;
    }

    // 3 cases
    val sameSuited = hand.filter { it.suit == tile.suit };
    val twoBelow = null != sameSuited.find { it.numeral == tile.numeral - 2 }
    val oneBelow = null != sameSuited.find { it.numeral == tile.numeral - 1 }
    val oneAbove = null != sameSuited.find { it.numeral == tile.numeral + 1 }
    val twoAbove = null != sameSuited.find { it.numeral == tile.numeral + 2 }

    return (twoBelow && oneBelow) || (oneBelow && oneAbove) || (oneAbove && twoAbove)
  }

}