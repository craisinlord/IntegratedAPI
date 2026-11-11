package com.craisinlord.integrated_api.world.terrainadaptation.beardifier;

import com.craisinlord.integrated_api.mixins.structures.BeardifierMixin;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

/**
 * Utility interface for use with {@link BeardifierMixin}.
 */
public interface EnhancedBeardifierData {
    ObjectListIterator<EnhancedBeardifierRigid> getEnhancedRigidIterator();
    void setEnhancedRigidIterator(ObjectListIterator<EnhancedBeardifierRigid> enhancedRigidIterator);
    ObjectListIterator<EnhancedJigsawJunction> getEnhancedJunctionIterator();
    void setEnhancedJunctionIterator(ObjectListIterator<EnhancedJigsawJunction> enhancedJunctionIterator);
}