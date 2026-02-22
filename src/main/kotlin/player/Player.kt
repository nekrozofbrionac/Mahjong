import de.nekrozofbrionac.mahjong.board.TileType
import de.nekrozofbrionac.player.control.PlayerControl

interface Player {
  val playerControl: PlayerControl

  val hand: List<TileType>;
  val melds: List<List<TileType>>

  fun sortHand()

  fun addToHand(tile: TileType);
  fun discard(tile: TileType): TileType?;

}