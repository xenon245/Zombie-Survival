package com.github.monulo.zombie

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

object ZombieSurvival {
    fun save(player: Player): YamlConfiguration {
        val yaml = YamlConfiguration()
        if(Zombie.zombie.contains(player.name)) {
            yaml.setRole("ROLE", "zombie")
        } else if(Zombie.survivers.contains(player.name)) {
            yaml.setRole("ROLE", "survivor")
        }
        if(Zombie.superzombie.contains(player.name)) {
            yaml.setRole("SUPERZOMBIE", true)
        } else {
            yaml.setRole("SUPERZOMBIE", false)
        }
        return yaml
    }
    private fun ConfigurationSection.setRole(name: String, role: String) {
        set(name, role)
    }
    private fun ConfigurationSection.setRole(name: String, boolean: Boolean) {
        set(name, boolean)
    }
    fun load(yaml: YamlConfiguration, player: Player) {
        yaml.loadRole(player)
    }
    private fun ConfigurationSection.loadRole(player: Player) {
        val role = get("ROLE")
        if(role == "zombie") {
            Zombie.zombie.add(player.name)
        } else if(role == "survivor") {
            Zombie.survivers.add(player.name)
        }
        val superzombie = get("SUPERZOMBIE")
        if(superzombie == true) {
            Zombie.superzombie.add(player.name)
        }
    }
}