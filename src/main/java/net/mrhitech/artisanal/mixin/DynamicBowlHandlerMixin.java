package net.mrhitech.artisanal.mixin;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.food.DynamicBowlHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.items.Food;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.mrhitech.artisanal.Artisanal;
import net.mrhitech.artisanal.common.item.ArtisanalItems;
import net.mrhitech.artisanal.util.mixininterface.IDynamicBowlHandler;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = DynamicBowlHandler.class, remap = false)
public abstract class DynamicBowlHandlerMixin extends FoodHandler.Dynamic implements IDynamicBowlHandler {
    
    
    @ModifyVariable(method = "onItemUse", at = @At("STORE"), ordinal = 0, remap = true) 
    private static ItemStack onItemUseMixin(ItemStack stack) {
        Artisanal.LOGGER.info("Entering method...");
        AtomicReference<ItemStack> stackBowl = new AtomicReference<ItemStack>(ItemStack.EMPTY);
        
        stack.getCapability(FoodCapability.CAPABILITY).ifPresent(cap -> {
            if (cap instanceof DynamicBowlHandler bowlHandler) {
                stackBowl.set(bowlHandler.getBowl());
            }
        });
        
        if (stack.equals(ItemStack.EMPTY)) {
            Artisanal.LOGGER.info("Stack is empty...");
            return stack;
        }
        else {
            Artisanal.LOGGER.info("Stack is not empty");
            ItemStack toReturn = new ItemStack(ArtisanalItems.DIRTY_BOWL.get());
            toReturn.getCapability(FoodCapability.CAPABILITY).ifPresent(cap -> {
                if (cap instanceof DynamicBowlHandler bowlHandler) {
                    bowlHandler.setBowl(stackBowl.get());
                    Artisanal.LOGGER.info("Set the bowl");
                }
            });
            Artisanal.LOGGER.info("Returning the set bowl");
            return toReturn;
        }
    }
    
}
