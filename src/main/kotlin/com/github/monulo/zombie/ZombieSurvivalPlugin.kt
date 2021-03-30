package com.github.monulo.zombie

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import com.github.monun.kommand.kommand
import com.github.monun.tap.effect.playFirework
import com.github.monun.tap.fake.FakeEntityServer
import io.papermc.paper.event.entity.EntityMoveEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.WorldSaveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.BoundingBox
import org.bukkit.util.NumberConversions
import java.io.File
import java.util.function.Predicate
import kotlin.random.Random

class ZombieSurvivalPlugin : JavaPlugin(), Listener, Runnable {
    companion object {
        val vaccine = ItemStack(Material.GOLDEN_APPLE).apply {
            itemMeta = itemMeta.apply { setDisplayName("${ChatColor.RESET}백신") }
        }
    }
    override fun onEnable() {
        if(Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") == null) {
            Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam("zombie")
        }
        if(Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") == null) {
            Bukkit.getScoreboardManager().mainScoreboard.registerNewTeam("survivor")
        }
        val zombieteam = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
        zombieteam.color(NamedTextColor.RED)
        val survivorteam = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
        survivorteam.color(NamedTextColor.AQUA)
        kommand {
            register("zs") {
                ZombieKommand.register(this)
            }
        }
        server.run {
            worlds.first().setGameRule(GameRule.REDUCED_DEBUG_INFO, true)
            worlds.first().setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            worlds.first().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            pluginManager.registerEvents(this@ZombieSurvivalPlugin, this@ZombieSurvivalPlugin)
            scheduler.runTaskTimer(this@ZombieSurvivalPlugin, this@ZombieSurvivalPlugin, 0L, 1L)
        }
        Bukkit.getWorlds().first().apply {
            val border = worldBorder
            border.center = Location(this, 0.0, 0.0, 0.0)
            border.size = 3000.0
            spawnLocation = getHighestBlockAt(0, 0).location
        }
        Zombie.fakeEntityServer = FakeEntityServer.create(this)
        registerRecipe()
        for(zom in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zom)!!
            Zombie.fakeEntityServer.addPlayer(zombie)
        }
        for(sur in Zombie.survivers) {
            val survivor = Bukkit.getPlayer(sur)!!
            Surviverlocation.addPlayer(survivor)
        }
    }

    override fun onDisable() {
        save()
    }
    @EventHandler
    fun onWorldSave(event: WorldSaveEvent) {
        save()
    }
    private fun save() {
        for(player in Bukkit.getOfflinePlayers()) {
            val yaml = ZombieSurvival.save(player as Player)
            val dataFolder = dataFolder.also { it.mkdirs() }
            val file = File(dataFolder, "${player.name}.yml")
            yaml.save(file)
        }
    }
    private fun registerRecipe() {
        val key = NamespacedKey(this, "vaccine")
        val recipe = ShapedRecipe(key, vaccine).apply {
            shape(
                "CMG",
                "ZLB",
                "RSP"
            )
            setIngredient('C', ItemStack(Material.GOLDEN_CARROT))
            setIngredient('M', ItemStack(Material.GLISTERING_MELON_SLICE))
            setIngredient('G', ItemStack(Material.GOLDEN_APPLE))
            setIngredient('Z', ItemStack(Material.ZOMBIE_HEAD))
            setIngredient('L', ItemStack(Material.GLASS_BOTTLE))
            setIngredient('B', ItemStack(Material.BLAZE_ROD))
            setIngredient('R', ItemStack(Material.RABBIT_FOOT))
            setIngredient('S', ItemStack(Material.NAUTILUS_SHELL))
            setIngredient('P', ItemStack(Material.PHANTOM_MEMBRANE))
        }
        Bukkit.addRecipe(recipe)
    }
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        Zombie.fakeEntityServer.addPlayer(event.player)
        for(zombie1 in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zombie1)!!
            Zombie.fakeEntityServer.addPlayer(zombie)
            val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
            zomt.addPlayer(zombie)
        }
        var survivors = arrayListOf<Player>()
        for(sur1 in Zombie.survivers) {
            val sur = Bukkit.getPlayer(sur1) ?: return
            survivors.add(sur)
            Surviverlocation.addPlayer(sur)
            val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
            surt.addPlayer(sur)
        }
    }
    @EventHandler
    fun onPaperServerListPingEvent(event: PaperServerListPingEvent) {
        event.motd = "${ChatColor.DARK_RED}${ChatColor.BOLD}S U R V I V A L"
        event.maxPlayers = 100
    }
    override fun run() {
        for(zombie1 in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zombie1) ?: return
            zombie.addPotionEffect(PotionEffect(PotionEffectType.POISON, 2, 1, true, false, false))
            if(!Bukkit.getServer().worlds.first().isDayTime) {
                zombie.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 2, 0, true, false, false))
                zombie.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 2, true, false, false))
            } else {
                zombie.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, 2, 0, true, false, false))
            }
        }
        for(zombie1 in Zombie.superzombie) {
            val zombie = Bukkit.getPlayer(zombie1) ?: return
            if(Zombie.survivers.contains(zombie.name)) {
                zombie.healthScale = 20.0
            } else {
                zombie.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 2, 0, false, false, false))
                zombie.healthScale = 10.0
            }
        }
        Zombie.fakeEntityServer.update()
        for(player in Bukkit.getOnlinePlayers()) {
            if(!Zombie.zombie.contains(player.name) && !Zombie.survivers.contains(player.name)) {
                val file = File(dataFolder, "${player.name}.yml").also { if(!it.exists()) return }
                val yaml = YamlConfiguration.loadConfiguration(file)
                ZombieSurvival.load(yaml, player)
            }
        }
        for(sur in Zombie.survivers) {
            val survivor = Bukkit.getPlayer(sur)!!
            Surviverlocation.update(survivor)
        }
    }
    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        event.respawnLocation = getSpawnLocation(event.player.name)
    }
    @EventHandler
    fun onCraft(event: CraftItemEvent) {
        for(zom in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zom)
            if(event.whoClicked === zombie) {
                if(event.recipe.result.isSimilar(vaccine)) {
                    event.isCancelled = true
                }
            }
        }
    }
    @EventHandler
    fun onPlayerEatVaccine(event: PlayerAttemptPickupItemEvent) {
        for(zom in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zom)
            if(zombie === event.player) {
                if(event.item.itemStack.isSimilar(vaccine)) {
                    Zombie.zombie.remove(event.player.name)
                    Zombie.survivers.add(event.player.name)
                    event.player.inventory.removeItem(vaccine)
                }
            }
        }
    }
    private fun getSpawnLocation(name: String): Location {
        val seed = name.hashCode()
        val random = Random(seed.toLong() xor 0x20070414)
        val world = Bukkit.getWorlds().first()
        val border = world.worldBorder
        val size = border.size / 2.0
        val x = random.nextDouble() * size - size / 2.0
        val z = random.nextDouble() * size - size / 2.0
        val block = world.getHighestBlockAt(NumberConversions.floor(x), NumberConversions.floor(z))
        return block.location.add(0.5, 1.0, 0.5)
    }
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        Zombie.fakeEntityServer.removePlayer(event.player)
        for(sur in Zombie.survivers) {
            val survivor = Bukkit.getPlayer(sur)
            if(event.player == survivor) {
                Surviverlocation.removePlayer(event.player)
            }
        }
    }
    @EventHandler
    fun onPlayerDamagedByEntity(event: EntityDamageByEntityEvent) {
        if(event.entity is Player) {
            val player = event.entity as Player
            for(surviver1 in Zombie.survivers) {
                val surviver = Bukkit.getPlayer(surviver1)
                if(surviver === player) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 5 * 20, 2, true, false, false))
                }
            }
        }
    }
    @EventHandler
    fun onPlayerDamaged(event: EntityDamageEvent) {
        if(event.entity is Player) {
            val player = event.entity as Player
            for(zombie1 in Zombie.zombie) {
                val zombie = Bukkit.getPlayer(zombie1)
                if(event.cause == EntityDamageEvent.DamageCause.FALL) {
                    if(player.inventory.helmet == null && player.inventory.chestplate == null && player.inventory.boots == null && player.inventory.leggings == null) {
                        if(zombie === player) {
                            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 5 * 20, 2, true, false, true))
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        var list = arrayListOf<Player>()
        for(zombie1 in Zombie.zombie) {
            Bukkit.getPlayer(zombie1)?.let { list.add(it) }
        }
        val item = event.item ?: return
        if(event.action == Action.RIGHT_CLICK_AIR) {
            for(zombie1 in Zombie.superzombie) {
                val zombie = Bukkit.getPlayer(zombie1)
                if(item.type == Material.DIAMOND) {
                    val player = event.player
                    if(zombie === player && player.getPotionEffect(PotionEffectType.POISON) != null) {
                        item.amount--
                        val list = arrayListOf<Player>()
                        for(zombies in Zombie.zombie) {
                            val zom = Bukkit.getPlayer(zombies) ?: return
                            list += zom
                        }
                        player.setCooldown(Material.DIAMOND, 10 * 20)
                        for(p in list) {
                            p.sendTitle("${ChatColor.RED}GRRR", "${ChatColor.WHITE}${player.name}님이 당신을 소환하려고 합니다.", 3, 20, 3)
                            p.playSound(p.location, Sound.ENTITY_ZOMBIE_AMBIENT, SoundCategory.MASTER, 2.0F, 1.0F)
                        }
                        Bukkit.getScheduler().runTaskLater(this, Summon(player, list), 2 * 20)
                    }
                } else if(item.type == Material.EMERALD) {
                    val player = event.player
                    if(zombie === player && player.getPotionEffect(PotionEffectType.POISON) != null) {
                        val random = Random.nextInt(Zombie.survivers.size)
                        val sur = Zombie.survivers[random]
                        val player2 = Bukkit.getPlayer(sur) ?: return
                        val task = Bukkit.getScheduler().runTaskTimer(this, TeleportToHuman(player2, event.player, event.player.location), 0L, 1L)
                        Bukkit.getScheduler().runTaskLater(this, task::cancel, 5 * 20)
                        event.item!!.amount--
                        player.setCooldown(Material.EMERALD, 10 * 20)
                    }
                } else if(item.type == Material.BOOK) {
                    val name = item.itemMeta.displayName
                    val player = Bukkit.getPlayer(name)!!
                    event.player.teleport(player.location)
                    event.player.world.strikeLightning(event.player.location)
                    item.amount--
                }
            }
            for(zombie1 in Zombie.zombie) {
                val zombie = Bukkit.getPlayer(zombie1)
                if(zombie === event.player && event.player.getPotionEffect(PotionEffectType.POISON) != null) {
                    if(!Zombie.superzombie.contains(zombie.name)) {
                        if(item.type == Material.DIAMOND) {
                            var list = arrayListOf<Player>()
                            for(sz in Zombie.superzombie) {
                                list.add(Bukkit.getPlayer(sz)!!)
                            }
                            list.forEach { z ->
                                if(Zombie.survivers.contains(z.name)) {
                                    list.remove(z)
                                }
                            }
                            val book = ItemStack(Material.BOOK).apply { itemMeta = itemMeta.apply { setDisplayName("${event.player.name}") }}
                            list.shuffled()[0].inventory.addItem(book)
                            item.amount--
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    fun onPlayerBlockPlace(event: BlockPlaceEvent) {
        for(zombie1 in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zombie1)
            if(zombie === event.player) {
                if(event.block.type == Material.OBSIDIAN) {
                    event.isCancelled = true
                }
            }
        }
    }
    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
    }
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        for(zombie1 in Zombie.zombie) {
            val zombie = Bukkit.getPlayer(zombie1)!!
            if(event.entity == zombie) {
                val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
                val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
                zomt.addPlayer(zombie)
                surt.removePlayer(zombie)
                val biome = Bukkit.getWorlds().first().getBiome(event.entity.location.x.toInt(), event.entity.location.y.toInt(),
                    event.entity.location.z.toInt()
                )
                if(biome.name.contains("DESERT")) {
                    event.drops += ItemStack(Material.RABBIT_FOOT)
                } else if(biome.name.contains("OCEAN")) {
                    event.drops += ItemStack(Material.NAUTILUS_SHELL)
                } else if(event.entity.world.environment == World.Environment.NETHER) {
                    event.drops += ItemStack(Material.BLAZE_ROD)
                } else if(biome.name.contains("JUNGLE")) {
                    event.drops += ItemStack(Material.ZOMBIE_HEAD)
                } else if(event.entity.world.environment == World.Environment.THE_END) {
                    event.drops += ItemStack(Material.END_CRYSTAL)
                } else if(biome.name.contains("FOREST")) {
                    event.drops += ItemStack(Material.GLISTERING_MELON_SLICE)
                } else {
                    event.drops += ItemStack(Material.PHANTOM_MEMBRANE)
                }
            }
        }
        for(survivor1 in Zombie.survivers) {
            val survivor = Bukkit.getPlayer(survivor1) ?: return
            val surt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("survivor") ?: return
            val zomt = Bukkit.getScoreboardManager().mainScoreboard.getTeam("zombie") ?: return
            surt.removePlayer(survivor)
            zomt.addPlayer(survivor)
            if(event.entity == survivor) {
                Zombie.fakeEntityServer.addPlayer(event.entity)
                for(zombie1 in Zombie.superzombie) {
                    val superzombie = Bukkit.getPlayer(zombie1)
                    if(event.entity === superzombie) {
                        Zombie.zombie.add(event.entity.name)
                        Zombie.survivers.remove(event.entity.name)
                        Zombie.superzombie.add(event.entity.name)
                        for(player in Bukkit.getOnlinePlayers()) {
                            player.sendTitle("${ChatColor.RED}${player.name}", "${ChatColor.RED}님이 슈퍼좀비가 되었습니다.", 5, 20, 5)
                        }
                    } else {
                        Zombie.survivers.remove(survivor.name)
                        Zombie.zombie.add(survivor.name)
                        for(player in Bukkit.getOnlinePlayers()) {
                            player.sendTitle("${ChatColor.RED}${player.name}", "${ChatColor.RED}님이 좀비가 되었습니다.", 5, 20, 5)
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        if(event.entityType != EntityType.DROPPED_ITEM && event.entityType != EntityType.PLAYER) {
            event.isCancelled = true
        }
    }
}
class Summon(val player: Player, val list : ArrayList<Player>) : Runnable {
    private var ticks = 0
    override fun run() {
        ticks++
        if(ticks > 40) {
            val firework = FireworkEffect.builder().withColor(Color.RED).withColor(Color.GREEN).with(FireworkEffect.Type.BALL_LARGE).build()
            player.location.world.playFirework(player.location, firework, 1.0)
            for(i in 1..10) {
                list.shuffled()[i - 1].teleport(player.location)
            }
            player.world.strikeLightning(player.location)
        }
    }
}
class TeleportToHuman(val target: Player, val player: Player, val location: Location) : Runnable {
    private var ticks = 0
    override fun run() {
        ticks++
        if(ticks == 1) {
            Surviverlocation.addSpectator(target)
        }
        player.teleport(Location(target.world, target.location.x, target.location.y + 1, target.location.z))
        player.gameMode = GameMode.SPECTATOR
        if(ticks >= 5 * 20 - 1) {
            Surviverlocation.removeSpectator(target)
            player.teleport(location)
            player.gameMode = GameMode.SURVIVAL
        }
    }
}