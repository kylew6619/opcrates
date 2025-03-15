package com.venned.simplecrates.build;

import com.venned.simplecrates.build.player.CrateKeyPlayer;

import java.util.List;

public class BackPack {

    List<CrateKeyPlayer> crateKeyPlayers;

    public BackPack(List<CrateKeyPlayer> crateKeyPlayers) {
        this.crateKeyPlayers = crateKeyPlayers;
    }

    public List<CrateKeyPlayer> getCrateKeys() {
        return crateKeyPlayers;
    }
}
