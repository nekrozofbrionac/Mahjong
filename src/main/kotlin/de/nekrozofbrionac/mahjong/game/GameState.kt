import de.nekrozofbrionac.mahjong.game.Game

interface GameState {
  fun onStateEntry(game: Game)

  fun disallowedStateAction() {
    throw UnsupportedOperationException("Unsupported or unimplemented state operation?!");
  }
}