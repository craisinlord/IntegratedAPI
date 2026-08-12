package com.craisinlord.integrated_api.misc.workstations;

import com.google.gson.annotations.Expose;
import net.minecraft.resources.Identifier;

public final class WorkstationObj {
    @Expose public String output = "";
    @Expose public int weight = 0;
    public transient Identifier outputBlock;

    public void setOutputBlock() {
        outputBlock = Identifier.tryParse(output);
    }
}
