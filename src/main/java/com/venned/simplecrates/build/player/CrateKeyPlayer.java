package com.venned.simplecrates.build.player;

import com.venned.simplecrates.build.crate.Crate;

public class CrateKeyPlayer {


    Crate crate;
    int keys;

    public CrateKeyPlayer(Crate crate, int keys) {
        this.crate = crate;
        this.keys = keys;
    }

    public Crate getCrate() {
        return crate;
    }

    public int getKeys() {
        return keys;
    }

    public void increment(int keys){
        this.keys += keys;
    }

    public void desIncrement(int keys){
        this.keys -= keys;
    }
}
