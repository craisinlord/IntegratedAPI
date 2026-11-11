package com.craisinlord.integrated_api.misc.workstations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.Identifier;

public final class WorkstationObj {
    @Expose @SerializedName(value = "required_mod", alternate = {"requiredMod"}) public String requiredMod = "minecraft";
    @Expose @SerializedName(value = "output_block", alternate = {"output", "outputBlock"}) public String output = "";
    @Expose public int weight = 1;
    public transient Identifier outputBlock;

    public void setOutputBlock() {
        outputBlock = Identifier.tryParse(output);
    }
}
