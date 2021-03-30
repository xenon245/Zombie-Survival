package com.github.monulo.zombie

import com.github.monun.tap.fake.FakeEntity
import com.github.monun.tap.fake.invisible
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object Surviverlocation {
    var fakeEntity = HashMap<Player, FakeEntity>()
    fun addPlayer(player: Player) {
        if(fakeEntity[player] == null) {
            fakeEntity[player] = Zombie.fakeEntityServer.spawnEntity(player.location, ArmorStand::class.java)!!
            fakeEntity[player]?.apply {
                updateMetadata<ArmorStand> {
                    invisible = true
                    isMarker = true
                    setGravity(false)
                    isGlowing = true
                }
                updateEquipment {
                    helmet = ItemStack(Material.PLAYER_HEAD)
                }
            }
        }
    }
    fun removePlayer(player: Player) {
        if(fakeEntity[player] != null) {
            fakeEntity[player]?.remove()
        }
    }
    fun update(player: Player) {
        if(fakeEntity[player] != null) {
            fakeEntity[player]?.moveTo(Location(player.world, player.location.x, player.location.y + 3, player.location.z))
        }
    }
}