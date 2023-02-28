import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StructureSearchResult;
import org.spigotmc.event.entity.EntityMountEvent;
import org.w3c.dom.events.Event;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

public class Main extends JavaPlugin implements Listener {
	HashMap<Player, Long> atePufferfishAt = new HashMap<Player, Long>();
	HashMap<Player, Boolean> drankMilkOrDied = new HashMap<Player, Boolean>();

	Advancement mobspawner = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:mobspawner", this));
	Advancement spyglass = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:spyglass", this));
	Advancement rabbitstew = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:rabbitstew", this));
	Advancement totem = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:totem", this));
	Advancement lactoseintolerant = Bukkit
			.getAdvancement(NamespacedKey.fromString("custom_advancements:lactoseintolerant", this));
	Advancement noteblock = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:noteblock", this));
	Advancement buildahouse = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:buildahouse", this));
	Advancement housingissuitable = Bukkit
			.getAdvancement(NamespacedKey.fromString("custom_advancements:housingsuitable", this));
	Advancement villagerAdv = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:villager", this));
	Advancement mobfarm = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:mobfarm", this));
	Advancement mines = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:mines", this));
	Advancement chestroom = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:chestroom", this));
	Advancement elytra = Bukkit.getAdvancement(NamespacedKey.fromString("custom_advancements:elytrafail", this));
	Advancement pyramid_explosion = Bukkit
			.getAdvancement(NamespacedKey.fromString("custom_advancements:pyramid_explosion", this));

	ItemStack spyglassItem = new ItemStack(Material.SPYGLASS);
	ItemStack rabbitstewItem = new ItemStack(Material.RABBIT_STEW);
	Plugin pl = this;

	Timer timer = new Timer();
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			atePufferfishAt.forEach((player, t) -> {
				if (drankMilkOrDied.get(player).equals(false)) {
					if ((System.currentTimeMillis() / 1000L) - 60 >= t) {
						Runnable rnbl = new Runnable() {
							public void run() {
								player.getAdvancementProgress(lactoseintolerant).awardCriteria("milk");
							}
						};
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(pl, rnbl);
						atePufferfishAt.remove(player);
						drankMilkOrDied.remove(player);
					}
				}
			});
		}
	};

	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, (Plugin) this);
		timer.scheduleAtFixedRate(task, 0, 5000);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent Event) {
		if (Event.getBlock().getType() == Material.MOSSY_COBBLESTONE) {
			Location location = Event.getBlock().getLocation();
			int range = 7, yRange = 2;
			Boolean fulfilled = false;
			for (int x = -range; x < range; x++) {
				for (int y = -yRange; y < yRange; y++) {
					for (int z = -range; z < range; z++) {
						Location loc = new Location(location.getWorld(), location.getX() + x, location.getY() + y,
								location.getZ() + z);
						Block block = loc.getBlock();
						if (block.getType() == Material.SPAWNER) {
							fulfilled = true;
							break;
						}
					}
				}
			}

			if (fulfilled == true) {
				Event.getPlayer().getAdvancementProgress(mobfarm).awardCriteria("farm");
			}
		}

		if (Event.getBlock().getType() == Material.SPAWNER) {
			AdvancementProgress progress = Event.getPlayer().getAdvancementProgress(mobspawner);
			if (!progress.isDone()) {
				Event.setCancelled(true);
				progress.awardCriteria("break");
			}
		}
	}

	@EventHandler
	public void onCraft(CraftItemEvent Event) {
		if (Event.getRecipe().getResult().equals(spyglassItem)) {
			Player holder = (Player) Event.getWhoClicked();
			holder.getAdvancementProgress(spyglass).awardCriteria("craft");
		}

		if (Event.getRecipe().getResult().equals(rabbitstewItem)) {
			Player player = (Player) Event.getWhoClicked();
			player.getAdvancementProgress(rabbitstew).awardCriteria("craft");
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent Event) {
		Player player = Event.getEntity().getPlayer();

		if (player.getInventory().contains(new ItemStack(Material.TOTEM_OF_UNDYING))) {
			player.getAdvancementProgress(totem).awardCriteria("death");
		}

		if (player.getLastDamageCause().getCause().equals(DamageCause.FALL)) {
			if (player.getInventory().contains(new ItemStack(Material.ELYTRA))) {
				if (player.getWorld().getEnvironment().equals(Environment.THE_END)) {
					StructureSearchResult searchResult = player.getWorld().locateNearestStructure(player.getLocation(),
							Structure.END_CITY, 16, false);
					if (searchResult.getStructure().equals(Structure.END_CITY)) {
						Location newLoc = player.getLocation();
						newLoc.setY(newLoc.getY() + player.getFallDistance());
						double distance = searchResult.getLocation().distance(newLoc);
						System.out.println(distance);
						if (distance <= 650) {
							player.getAdvancementProgress(elytra).awardCriteria("fall");
						}
					}
				}
			}
		}

		if (player.getLastDamageCause().getCause().equals(DamageCause.ENTITY_EXPLOSION)
				|| player.getLastDamageCause().getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
			if (player.getWorld().getEnvironment().equals(Environment.NORMAL)) {
				StructureSearchResult searchResult = player.getWorld().locateNearestStructure(player.getLocation(),
						Structure.DESERT_PYRAMID, 16, false);
				if (searchResult.getStructure().equals(Structure.DESERT_PYRAMID)) {
					double distance = searchResult.getLocation().distance(player.getLocation());
					System.out.println(distance);
					if (distance <= 80) {
						player.getAdvancementProgress(pyramid_explosion).awardCriteria("death");
					}
				}
			}
		}

		if (!atePufferfishAt.containsKey(player))
			return;
		drankMilkOrDied.put(player, true);
	}

	@EventHandler
	public void onEat(PlayerItemConsumeEvent Event) {
		Player player = Event.getPlayer();
		AdvancementProgress progress = Event.getPlayer().getAdvancementProgress(lactoseintolerant);
		if (progress.isDone())
			return;
		if (Event.getItem().getType() == Material.PUFFERFISH) {
			atePufferfishAt.put(player, System.currentTimeMillis() / 1000L);
			drankMilkOrDied.put(player, false);
		}

		if (Event.getItem().getType() == Material.MILK_BUCKET) {
			if (!atePufferfishAt.containsKey(player))
				return;
			drankMilkOrDied.put(player, true);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent Event) {
		if (!Event.getAction().equals(Action.LEFT_CLICK_BLOCK))
			return;
		if (Event.getClickedBlock().getType() == Material.NOTE_BLOCK) {
			Event.getPlayer().getAdvancementProgress(noteblock).awardCriteria("play");
		}
	}

	public boolean checkBlocks(Block block) {
		return block.getType().name().contains("_DOOR") || block.getType() == Material.CRAFTING_TABLE
				|| block.getType() == Material.FURNACE || block.getType() == Material.CHEST;
	}

	public boolean checkExtraBlocks(Block block) {
		return block.getType().name().contains("_STAIRS")
				|| (block.getType().name().contains("_FENCE") && !block.getType().name().contains("_FENCE_GATE"))
				|| block.getType().name().contains("_PRESSURE_PLATE");
	}

	public Boolean[] checkNearbyBlocks(Location location) {
		int range = 7, yRange = 2;
		// door, ct, furnace, chest, stair1, str2, fence, pressureplate
		Boolean[] fulfilled = { false, false, false, false, false, false, false, false };
		for (int x = -range; x < range; x++) {
			for (int y = -yRange; y < yRange; y++) {
				for (int z = -range; z < range; z++) {
					Location loc = new Location(location.getWorld(), location.getX() + x, location.getY() + y,
							location.getZ() + z);
					Block block = loc.getBlock();
					if (block.getType().name().contains("_DOOR")) {
						fulfilled[0] = true;
					}
					if (block.getType() == Material.CRAFTING_TABLE) {
						fulfilled[1] = true;
					}
					if (block.getType() == Material.FURNACE) {
						fulfilled[2] = true;
					}
					if (block.getType() == Material.CHEST) {
						fulfilled[3] = true;
					}
					if (block.getType().name().contains("_STAIRS")) {
						if (fulfilled[4] == true) {
							fulfilled[5] = true;
						}
						fulfilled[4] = true;
					}
					if ((block.getType().name().contains("_FENCE")
							&& !block.getType().name().contains("_FENCE_GATE"))) {
						fulfilled[6] = true;
					}
					if (block.getType().name().contains("_PRESSURE_PLATE")) {
						fulfilled[7] = true;
					}
				}
			}
		}
		return fulfilled;
	}

	@EventHandler
	public void onEntityMount(EntityMountEvent Event) {
		Entity supposedVillager = Event.getEntity();
		Entity entity = Event.getMount();
		if (supposedVillager.getType() == EntityType.VILLAGER && entity.getType() == EntityType.BOAT) {
			for (Entity nearbyEntity : entity.getNearbyEntities(10, 10, 10)) {
				if (nearbyEntity.getType() == EntityType.PLAYER) {
					Player player = (Player) nearbyEntity;
					if(player.getAdvancementProgress(villagerAdv).isDone()) return;
					player.sendMessage("AndrewTate has made the advancement " + ChatColor.GREEN + "[Human Trafficking]");
					player.getAdvancementProgress(villagerAdv).awardCriteria("boat");
				}
			}
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent Event) {
		Block block = Event.getBlock();
		Location loc = Event.getBlock().getLocation();
		AdvancementProgress progress_buildahouse = Event.getPlayer().getAdvancementProgress(buildahouse);
		AdvancementProgress progress_housingissuitable = Event.getPlayer().getAdvancementProgress(housingissuitable);
		AdvancementProgress progress_mines = Event.getPlayer().getAdvancementProgress(mines);
		AdvancementProgress progress_chestroom = Event.getPlayer().getAdvancementProgress(chestroom);

		if (!progress_chestroom.isDone()) {
			if (block.getType() == Material.CHEST) {
				int range = 7, yRange = 4;
				int chests = 0;
				for (int x = -range; x < range; x++) {
					for (int y = -yRange; y < yRange; y++) {
						for (int z = -range; z < range; z++) {
							Location lc = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y,
									loc.getZ() + z);
							Block bl = lc.getBlock();
							if (bl.getType() == Material.CHEST) {
								chests++;
								break;
							}
						}
					}
				}

				if (chests >= 6) {
					progress_chestroom.awardCriteria("chest");
				}
			}
		}

		if (!progress_mines.isDone()) {
			if (block.getType() == Material.COBBLESTONE_STAIRS) {
				Location location = Event.getBlock().getLocation();
				int range = 3, yRange = 7;
				int fulfilled = 0;
				for (int x = -range; x < range; x++) {
					for (int y = -yRange; y < yRange; y++) {
						for (int z = -range; z < range; z++) {
							Location lc = new Location(location.getWorld(), location.getX() + x, location.getY() + y,
									location.getZ() + z);
							Block bl = lc.getBlock();
							if (bl.getType() == Material.COBBLESTONE_STAIRS) {
								fulfilled++;
								break;
							}
						}
					}
				}

				if (fulfilled >= 14) {
					progress_mines.awardCriteria("stairs");
				}
			}
		}

		if (!progress_buildahouse.isDone()) {
			if (!checkBlocks(block))
				return;
			Boolean[] arr = checkNearbyBlocks(loc);
			if (arr[0] && arr[1] && arr[2] && arr[3]) {
				progress_buildahouse.awardCriteria("home");
			}
		} else {
			if (!progress_housingissuitable.isDone() && checkExtraBlocks(block)) {
				Boolean[] arr = checkNearbyBlocks(loc);
				if (arr[0] && arr[1] && arr[2] && arr[3] && arr[4] && arr[5] && arr[6] && arr[7]) {
					progress_housingissuitable.awardCriteria("home");
				}
			}
		}
	}

	public void onDisable() {
		timer.cancel();
	}
}
