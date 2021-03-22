package com.github.monulo.zombie

import com.github.monun.tap.fake.FakeEntityServer
import org.bukkit.entity.Player

object Zombie {
    var survivers = arrayListOf<String>()
    var zombie = arrayListOf<String>()
    var superzombie = arrayListOf<String>()
    lateinit var fakeEntityServer: FakeEntityServer
}