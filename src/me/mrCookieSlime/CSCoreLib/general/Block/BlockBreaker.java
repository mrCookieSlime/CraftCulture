package me.mrCookieSlime.CSCoreLib.general.Block;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockBreaker {
	
	public static void breakBlock(Player p, Block b) {
			BlockBreakEvent event = new BlockBreakEvent(b, p);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				event.setCancelled(true);
				for (ItemStack drop : b.getDrops()) {
					b.getWorld().dropItem(b.getLocation(), drop);
			    }
			    b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
			    b.setType(Material.AIR);
			}
	}
	
	public static void nullify(Block b) {
		b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
		b.setType(Material.AIR);
	}
	
	public static void breakBlock(Player p, Block b, List<ItemStack> drops) {
		BlockBreakEvent event = new BlockBreakEvent(b, p);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			event.setCancelled(true);
			for (ItemStack drop : drops) {
				b.getWorld().dropItem(b.getLocation(), drop);
		    }
		    b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getType());
		    b.setType(Material.AIR);
		}
}

}
