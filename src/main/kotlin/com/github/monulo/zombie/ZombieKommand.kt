package com.github.monulo.zombie

import com.github.monun.kommand.KommandBuilder
import com.github.monun.kommand.argument.player
import com.github.monun.kommand.sendFeedback
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ZombieKommand {
    fun register(builder: KommandBuilder) {
        builder.apply {
            then("attach") {
                then("player" to player()) {
                    then("zombie") {
                        executes {
                            zombie(it.parseArgument("player"), it.sender)
                        }
                    }
                    then("superzombie") {
                        executes {
                            superzombie(it.parseArgument("player"), it.sender)
                        }
                    }
                    then("survivor") {
                        executes {
                            survivor(it.parseArgument("player"), it.sender)
                        }
                    }
                }
            }
        }
    }
    private fun superzombie(player: Player, sender: CommandSender) {
        Zombie.run {
            superzombie.add(player.name)
        }
        for(zombie1 in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zombie1)!!
            val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
            val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
            zomt.addPlayer(zombie)
            surt.removePlayer(zombie)
        }
        for(sur1 in Zombie.survivers) {
            val sur = Bukkit.getPlayer(sur1)!!
            val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
            val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
            zomt.removePlayer(sur)
            surt.addPlayer(sur)
        }
        sender.sendFeedback("${player.name}님을 슈퍼좀비로 만들었습니다.")
    }
    private fun zombie(player: Player, sender: CommandSender) {
        Surviverlocation.removePlayer(player)
        Zombie.fakeEntityServer.addPlayer(player)
        Zombie.run {
            survivers.remove(player.name)
            zombie.add(player.name)
            superzombie.remove(player.name)
        }
        for(zombie1 in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zombie1)!!
            val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
            val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
            zomt.addPlayer(zombie)
            surt.removePlayer(zombie)
        }
        for(sur1 in Zombie.survivers) {
            val sur = Bukkit.getPlayer(sur1)!!
            val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
            val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
            zomt.removePlayer(sur)
            surt.addPlayer(sur)
        }
        sender.sendFeedback("${player.name}님을 좀비로 만들었습니다.")
    }
    private fun survivor(player: Player, sender: CommandSender) {
        Surviverlocation.addPlayer(player)
        Zombie.run {
            survivers.add(player.name)
            zombie.remove(player.name)
            superzombie.remove(player.name)
        }
        Zombie.fakeEntityServer.removePlayer(player)
        for(zombie1 in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zombie1)!!
            val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
            val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
            zomt.addPlayer(zombie)
            surt.removePlayer(zombie)
        }
        for(sur1 in Zombie.survivers) {
            val sur = Bukkit.getPlayer(sur1)!!
            val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
            val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
            zomt.removePlayer(sur)
            surt.addPlayer(sur)
        }
        sender.sendFeedback("${player.name}님을 생존자로 만들었습니다.")
    }
}