package com.venned.simplecrates.utils;

import com.venned.simplecrates.build.crate.CrateBlock;
import com.venned.simplecrates.build.player.PlayerOpening;
import org.bukkit.entity.Player;

import java.util.*;

public class MapUtils {

    public static Set<PlayerOpening> playerOpenings = new HashSet<PlayerOpening>();
    public static Map<UUID, CrateBlock> playerOpenVirtual = new HashMap<UUID, CrateBlock>();
}
