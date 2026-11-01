package dev.pricetags;

import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class PriceTagsMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PriceConfig.load();
		ItemTooltipCallback.EVENT.register(PriceTagsMod::onTooltip);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("prices")
						.then(ClientCommandManager.literal("reload").executes(ctx -> {
							PriceConfig.load();
							ctx.getSource().sendFeedback(
									Component.literal("\u00a7aLoaded " + PriceConfig.size() + " prices"));
							return 1;
						}))
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Component.literal(
									"\u00a7eFile: " + PriceConfig.FILE + " | /prices reload"));
							return 1;
						})));
	}

	private static void onTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag type,
			List<Component> lines) {
		if (lines.isEmpty()) {
			return;
		}
		String price = PriceConfig.find(stack);
		if (price == null) {
			return;
		}

		Component line = Component.literal("~").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("$").withStyle(ChatFormatting.GREEN))
				.append(Component.literal(" " + price).withStyle(ChatFormatting.WHITE));

		lines.add(1, line); // сразу под названием предмета
	}
}
