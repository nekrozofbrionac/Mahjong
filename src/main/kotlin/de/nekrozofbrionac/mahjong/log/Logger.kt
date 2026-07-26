package de.nekrozofbrionac.mahjong.log

enum class LogRole {
  GAME,
}

object Logger {
  var level: Int = 0
  var displayedRoles: MutableSet<LogRole> = LogRole.entries.toMutableSet()

  fun log(role: LogRole, content: String, level: Int = 0) {
    if (!displayedRoles.contains(role)) {
      return;
    }
    if (level < this.level) {
      return;
    }

    print(content);
  }
}