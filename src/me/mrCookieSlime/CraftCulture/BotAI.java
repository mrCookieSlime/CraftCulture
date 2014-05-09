package me.mrCookieSlime.CraftCulture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.mrCookieSlime.CraftCulture.Utilities.BlockAdjacents;
import me.mrCookieSlime.CraftCulture.Utilities.BlockUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import com.adamki11s.pathing.AStar;
import com.adamki11s.pathing.AStar.InvalidPathException;
import com.adamki11s.pathing.PathingResult;
import com.adamki11s.pathing.Tile;

public class BotAI {
	
	public static main plugin;
	
	public BotAI(main instance) {
		plugin = instance;
	}
	
	static Map<Villager, List<Location>> moves = new HashMap<Villager, List<Location>>();
	
	public static void setLocation(Villager v, Location l) {
		Villagers.locations.put(v, l);
	}
	
	public static Location findClosePlace(Villager v, int radius) {
		
		Location l = Villagers.getHomePoint(v);
		
		Location place = null;
		
		for (int x = -(radius); x <= radius; x++) {
			for (int z = -(radius); z <= radius; z++) {
				for (int y = 0; y < l.getWorld().getMaxHeight(); y++) {
					Block current = l.getBlock().getRelative(x, y, z);
					if (current.getType().isSolid() && l.getBlock().getRelative(x, y + 1, z).getType() == Material.AIR && !(BlockAdjacents.hasAdjacentMaterial(current.getRelative(BlockFace.UP), Material.CHEST))) {
						if (current.getRelative(BlockFace.UP).getLocation().distanceSquared(l) <= radius) {
							place = current.getRelative(BlockFace.UP).getLocation();
							break;
						}
					}
				}
			}
		}
		return place;
	}
	
	public static void getCloseTo(Villager v, Location l, int radius) {
		for (int x = -(radius); x <= radius; x++) {
			for (int z = -(radius); z <= radius; z++) {
				for (int y = 0; y < l.getWorld().getMaxHeight(); y++) {
					Block current = l.getBlock().getRelative(x, y, z);
					if (current.getType().isSolid() 
						&& l.getBlock().getRelative(x, y + 1, z).getType() == Material.AIR
						&& l.getBlock().getRelative(x, y + 2, z).getType() == Material.AIR) {
						if (current.getRelative(BlockFace.UP).getLocation().distanceSquared(l) <= radius) {
							
							walkTo(v, current.getRelative(BlockFace.UP).getLocation());
							
							return;
						}
					}
				}
			}
		}
	}
	
	public static void addResourceGoal(Villager v, Material m, int amount) {
		Map<Material, Integer> resources = getResourceTask(v);
		
		if (resources.containsKey(v)) {
			amount = amount + resources.get(v);
		}
		
		resources.put(m, amount);
		
		Villagers.resources.put(v, resources);
		
		if (!Villagers.resourceIndex.get(v).contains(m)) {
			Villagers.resourceIndex.get(v).add(m);
		}
	}
	
	public static void goHome(Villager v) {
		walkTo(v, Villagers.getHomePoint(v));
	}
	
	public static void walkTo(Villager v, Location l) {
		List<Location> locations = new ArrayList<Location>();
		
		Location start = v.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
		Location finish = l.getBlock().getRelative(BlockFace.DOWN).getLocation();
		
		AStar path = null;
		try {
			path = new AStar(start, finish, plugin.getConfig().getInt("bots.work-area") * 2);
		} catch (InvalidPathException e) {
			System.out.println("Path Generation failed for Villager \"" + v.getCustomName() + "\"");
			System.out.println("InvalidPathException: " + e.getMessage());
			System.out.println(e.getCause());
			
			if(e.isEndNotSolid()){
	            System.out.println("End block is not walkable");
	        }
	        if(e.isStartNotSolid()){
	            System.out.println("Start block is not walkable");
	        }
		}
		if (path != null) {
			 ArrayList<Tile> route = path.iterate();
		        PathingResult result = path.getPathingResult();
		        
		        if (result == PathingResult.SUCCESS)  {
		        	for (Tile tile: route) {
		        		locations.add(tile.getLocation(start).getBlock().getRelative(BlockFace.UP).getLocation());
		        		locations.add(tile.getLocation(start).getBlock().getRelative(BlockFace.UP).getLocation());
		        		locations.add(tile.getLocation(start).getBlock().getRelative(BlockFace.UP).getLocation());
		        		locations.add(tile.getLocation(start).getBlock().getRelative(BlockFace.UP).getLocation());
		        		locations.add(tile.getLocation(start).getBlock().getRelative(BlockFace.UP).getLocation());
		        		locations.add(tile.getLocation(start).getBlock().getRelative(BlockFace.UP).getLocation());
		        		locations.add(tile.getLocation(start).getBlock().getRelative(BlockFace.UP).getLocation());
		        		locations.add(tile.getLocation(start).getBlock().getRelative(BlockFace.UP).getLocation());
		        	}
		        }
		        else {
		        	System.out.println("Path Generation failed for Villager \"" + v.getCustomName() + "\"");
		        }
		}
		else {
			System.out.println("Path Generation failed for Villager \"" + v.getCustomName() + "\"");
		}
		
		moves.put(v, locations);
	}
	
	public static void chat(Villager v, String message) {
		String msg = plugin.getConfig().getString("messages.chat");
		msg = msg.replace("%message%", message);
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}
	
	public static void askFor(Villager v, Material m, int amount) {
		String msg = plugin.getConfig().getString("messages.ask-item");
		msg = msg.replace("%amount%", String.valueOf(amount));
		msg = msg.replace("%item%", m.toString().toLowerCase().replace("_", " ") + "/s");
		chat(v, msg);
	}
	
	public static void breakBlock(Villager v, Block b) {
		if (b != null) {
			if (v.getLocation().distanceSquared(b.getLocation()) <= 4) {
				if (plugin.getConfig().getBoolean("bots.log-activity")) {
					System.out.println(Villagers.getName(v) + " just broke 1 " + b.getType().toString());
				}
				b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
				b.setType(Material.AIR);
				for (ItemStack item: b.getDrops()) {
					if (getAvailableChest(v) != null) {
						depositItem(v, getAvailableChest(v), item);
					}
					else {
						ItemStack ItemInHand = getCarriedItem(v);
						if (ItemInHand != null) {
							if (ItemInHand.getType() == item.getType() && ItemInHand.getAmount() < ItemInHand.getType().getMaxStackSize()) {
								ItemInHand.setAmount(ItemInHand.getAmount() + item.getAmount());
								Villagers.item.put(v, ItemInHand);
							}
						}
						else {
							Villagers.item.put(v, item);
						}
					}
				}
			}
			else {
				if (!hasMovingTask(v)) {
					getCloseTo(v, b.getLocation(), 4);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void placeBlock(Villager v, Material block, byte data, Location l) {
		if (v.getLocation().distanceSquared(l) <= 4) {
			if (plugin.getConfig().getBoolean("bots.log-activity")) {
				System.out.println(Villagers.getName(v) + " just placed 1 " + block.toString());
			}
			l.getBlock().setType(block);
			l.getBlock().setData(data);
			l.getWorld().playEffect(l, Effect.STEP_SOUND, block);
		}
		else {
			if (!hasMovingTask(v)) {
				getCloseTo(v, l, 4);
			}
		}
	}
	
	public static Chest getAvailableChest(Villager v) {
		Chest chest = null;
		
		for (Block b: Villagers.getChests(v)) {
			if (b.getType() == Material.CHEST) {
				Chest c = (Chest) b.getState();
				if (c.getInventory().getContents().length < c.getInventory().getSize()) {
					chest = c;
					break;
				}
			}
		}
		
		return chest;
	}
	
	public static void depositItem(Villager v, Chest chest, ItemStack item) {
		if (v.getLocation().distanceSquared(chest.getLocation()) <= 4) {
			chest.getWorld().playSound(chest.getLocation(), Sound.CHEST_OPEN, 1, 1);
			
			chest.getInventory().addItem(item);
			
			chest.getWorld().playSound(chest.getLocation(), Sound.CHEST_CLOSE, 1, 1);
		}
		else {
			getCloseTo(v, chest.getLocation(), 4);
			depositItem(v, chest, item);
		}
	}
	
	public static boolean hasMovingTask(Villager v) {
		return moves.containsKey(v);
	}
	
	public static List<Location> getNextPositions(Villager v) {
		return moves.get(v);
	}
	
	public static Location getNextPositionToWalk(Villager v) {
		List<Location> locs = getNextPositions(v);
		
		Location next = null;
		if (locs.size() == 0) {
			moves.remove(v);
		}
		else {
			next = locs.get(0).clone();
			locs.remove(0);
			moves.put(v, locs);
		}
		
		if (next != null) {
			setLocation(v, next);
		}
		
		return next;
	}
	
	public static void getAvailableItems(Villager v, List<Material> types, Map<Material, Integer> inv) {
		for (Chest c: BlockUtils.castToChest(Villagers.getChests(v))) {
			for (ItemStack item: c.getInventory().getContents()) {
				if (item != null) {
					if (types.contains(item.getType())) {
						types.add(item.getType());
						inv.put(item.getType(), item.getAmount());
					}
					else {
						inv.put(item.getType(), item.getAmount() + inv.get(item.getType()));
					}
				}
			}
		}
	}
	
	public static Location getCurrentLocation(Villager v) {
		return Villagers.locations.get(v);
	}
	
	public static List<UUID> getAngryPlayers(Villager v) {
		return Villagers.angry.get(v);
	}
	
	public static Map<Material, Integer> getResourceTask(Villager v) {
		return Villagers.resources.get(v);
	}
	
	public static Material getNextResourceGoal(Villager v) {
		if (Villagers.resourceIndex.size() > 0) {
			return Villagers.resourceIndex.get(v).get(0);
		}
		else {
			return null;
		}
	}
	
	public static int getResourceAmount(Villager v, Material m) {
		if (Villagers.resourceIndex.size() > 0) {
			return Villagers.resources.get(v).get(m);
		}
		else {
			return 0;
		}
	}
	
	public static Map<EntityType, Integer> getDropTask(Villager v) {
		return Villagers.drops.get(v);
	}
	
	public static void getNextDropGoal(Villager v, EntityType m, Integer amount) {
		m = Villagers.dropIndex.get(v).get(0);
		amount = getResourceTask(v).get(m);
	}
	
	public static boolean isAngryOn(Villager v, LivingEntity n) {
		if (n instanceof Player) {
			return getAngryPlayers(v).contains(n.getUniqueId());
		}
		else if (n instanceof Monster){
			return true;
		}
		else if (n instanceof Animals) {
			EntityType goal = null;
			getNextDropGoal(v, goal, null);
			if (goal == n.getType()) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	@Deprecated
	public static Block findClosestMaterial(Villager v, Material type) {
		int radius = plugin.getConfig().getInt("bots.work-area");
		Location center = Villagers.getHomePoint(v);
		
		Block closest = null;
		
		for (int x = -(radius); x <= radius; x++) {
			for (int y = -(radius); y <= radius; y++) {
				for (int z = -(radius); z <= radius; z++) {
					if (center.getBlock().getRelative(x, y, z).getType() == type) {
						if (closest == null) {
							closest = center.getBlock().getRelative(x, y, z);
						}
						else if (center.getBlock().getRelative(x, y, z).getLocation().distanceSquared(center) < closest.getLocation().distanceSquared(center)) {
							closest = center.getBlock().getRelative(x, y, z);
						}
					}
				}
			}
		}
		
		return closest;
	}
	
	public static ItemStack getCarriedItem(Villager v) {
		return Villagers.item.get(v);
	}
	
	public static int hasItemAvailable(Villager v, Material type) {
		
		int amount = 0;
		
		for (Chest c: BlockUtils.castToChest(Villagers.getChests(v))) {
			for (ItemStack item: c.getInventory().getContents()) {
				if (item != null) {
					if (item.getType() == type) {
						amount = amount + item.getAmount();
					}
				}
			}
		}
		
		ItemStack item = getCarriedItem(v);
		
		if (item != null) {
			if (item.getType() == type) {
				amount = amount + item.getAmount();
			}
		}
		
		return amount;
	}
	
	public static void scanArea(Villager v, Location l, int radius) {
		if (Villagers.mapped.size() > 0) {
			Villagers.map.clear();
			Villagers.mapping.clear();
			Villagers.mapped.clear();
		}
		
		World world = l.getWorld();
		Location current;
		
		for (int x = -(radius); x <= radius; x++) {
			for (int y = -(radius); y <= radius; y++) {
				for (int z = -(radius); z <= radius; z++) {
					current = new Location(world, x, y, z);
					List<Location> list = new ArrayList<Location>();
					if (Villagers.mapped.containsKey(v)) {
						list = Villagers.mapped.get(v);
					}
					list.add(current);
					Villagers.mapped.put(v, list);
					Map<Location, Material> map = new HashMap<Location, Material>();
					map.put(current, current.getBlock().getType());
					Villagers.mapping.put(v, map);
				}
			}
		}
		
		for (Location loc: Villagers.mapped.get(v)) {
			List<Material> list = new ArrayList<Material>();
			if (Villagers.foundmats.containsKey(v)) {
				list = Villagers.foundmats.get(v);
			}
			if (list.contains(loc.getBlock().getType()) && loc.getBlock().getType() != null && loc.getBlock().getType() != Material.AIR) {
				list.add(loc.getBlock().getType());
			}
			Villagers.foundmats.put(v, list);
		}
		
	}

}