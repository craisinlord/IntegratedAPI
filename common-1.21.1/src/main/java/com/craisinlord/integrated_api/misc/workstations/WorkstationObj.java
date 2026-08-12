package com.craisinlord.integrated_api.misc.workstations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

public class WorkstationObj {
    @Expose
    @SerializedName("required_mod")
    public String requiredMod = "minecraft";

    @Expose
    @SerializedName("output_block")
    public String outputBlockId;

    @Expose
    public int weight = 1;

    public transient ResourceLocation outputBlock = null;

    public void setOutputBlock() throws Exception {
        this.outputBlock = ResourceLocation.tryParse(outputBlockId);
        if (this.outputBlock == null) {
            throw new Exception("Error: " + outputBlockId + " is not a valid block ID!");
        }
    }
}
