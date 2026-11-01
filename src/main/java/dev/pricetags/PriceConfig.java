package dev.pricetags;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class PriceConfig {
	public static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pricetags.txt");

	private static final Map<String, String> PRICES = new HashMap<>();

	private static final String DEFAULT = """
			# Price Tags
			# Формат: предмет=цена
			# Предмет можно писать по-английски, по id или так, как он называется в игре.
			# После правки файла напиши в игре: /prices reload
			Elytra=400K
			Totem of Undying=70K
			""";

	private PriceConfig() {
	}

	public static int size() {
		return PRICES.size();
	}

	public static void load() {
		PRICES.clear();
		try {
			if (!Files.exists(FILE)) {
				Files.writeString(FILE, DEFAULT);
			}
			for (String raw : Files.readAllLines(FILE)) {
				String line = raw.replace("\uFEFF", "").trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				int eq = line.indexOf('=');
				if (eq <= 0) {
					continue;
				}
				String key = normalize(line.substring(0, eq));
				String value = line.substring(eq + 1).trim().replaceFirst("^[~$\\s]+", "");
				if (!key.isEmpty() && !value.isEmpty()) {
					PRICES.put(key, value);
				}
			}
		} catch (IOException ignored) {
		}
	}

	private static String normalize(String s) {
		String n = s.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
		if (n.startsWith("minecraft:")) {
			n = n.substring("minecraft:".length());
		}
		return n;
	}

	/** Возвращает цену для предмета или null. */
	public static String find(ItemStack stack) {
		if (PRICES.isEmpty()) {
			return null;
		}
		Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());

		String byPath = PRICES.get(normalize(id.getPath()));
		if (byPath != null && id.getNamespace().equals("minecraft")) {
			return byPath;
		}
		String byFull = PRICES.get(normalize(id.toString()));
		if (byFull != null) {
			return byFull;
		}
		if (byPath != null) {
			return byPath;
		}
		return PRICES.get(normalize(stack.getHoverName().getString()));
	}
}
