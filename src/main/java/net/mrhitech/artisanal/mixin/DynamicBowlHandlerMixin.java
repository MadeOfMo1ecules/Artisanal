package net.mrhitech.artisanal.mixin;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.food.DynamicBowlHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.items.Food;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemHandlerHelper;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = DynamicBowlHandler.class, remap = false)
public abstract class DynamicBowlHandlerMixin extends FoodHandler.Dynamic implements IDynamicBowlHandler {
    
    // Next line doesn't work, so band-aid solution
    // @ModifyVariable(method = "onItemUse", at = @At("STORE"), ordinal = 0, remap = false) 
    private static ItemStack onItemUseMixin(ItemStack stack) {
        Artisanal.LOGGER.info("Entering method...");
        
        if (stack.equals(ItemStack.EMPTY)) {
            Artisanal.LOGGER.info("Stack is empty...");
            return stack;
        }
        else {
            Artisanal.LOGGER.info("Stack is not empty");
            ItemStack toReturn = new ItemStack(ArtisanalItems.DIRTY_BOWL.get());
            toReturn.getCapability(FoodCapability.CAPABILITY).ifPresent(cap -> {
                if (cap instanceof DynamicBowlHandler bowlHandler) {
                    bowlHandler.setBowl(stack);
                    Artisanal.LOGGER.info("Set the bowl");
                }
            });
            Artisanal.LOGGER.info("Returning the set bowl");
            return toReturn;
        }
    }
    
    @Inject(method = "onItemUse", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onItemUseInject(ItemStack original, ItemStack result, LivingEntity entity, CallbackInfoReturnable<ItemStack> info)
    {
        // This is a rare stackable-with-remainder-after-finished-using item
        // See: vanilla honey bottles
        if (entity instanceof ServerPlayer player)
        {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, original);
            player.awardStat(Stats.ITEM_USED.get(original.getItem()));
        }
        
        // Pull the bowl out first, before we shrink the stack in super.finishUsingItem()
        final ItemStack bowl = onItemUseMixin(FoodCapability.get(original) instanceof DynamicBowlHandler handler ? handler.getBowl() : ItemStack.EMPTY);
        
        if (result.isEmpty())
        {
            info.setReturnValue(bowl);
            info.cancel();
            return;
        }
        else if (entity instanceof Player player && !player.getAbilities().instabuild)
        {
            // In non-creative, we still need to give the player an empty bowl, but we must also return the result here, as it is non-empty
            // The super() call to finishUsingItem will handle decrementing the stack - only in non-creative - for us already.
            ItemHandlerHelper.giveItemToPlayer(player, bowl);
        }
        info.setReturnValue(result);
        info.cancel();
        return;
    }
    
}
