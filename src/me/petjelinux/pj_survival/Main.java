package me.petjelinux.pj_survival;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener{
	/*
	 * Each time you call iterator() on a standard collection, a new Iterator is created. 
	 * Otherwise it would be impossible to have multiple iterators which point to different elements of the same collection.
	 */
	
	
	public static HashMap<Location, ArmorStand> Armor_Map = new HashMap<Location, ArmorStand>();
	
	@Override
	// 20 ticks == 1sec
	public void onEnable(){
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getLogger().info("PJ_Survival start!");
		Bukkit.getScheduler().runTaskTimer(this, new Runnable(){
			@Override
			public void run(){
				Bukkit.getLogger().info("PJ_Survival Schedule run!");
				checkChunks();
			}
		}, 600, 12000); //30sec start to run, 10min rerun
	}
	
	@Override
	public void onDisable(){
		Bukkit.getLogger().info("PJ_Survival disable!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return false;
	}
	
	public static void checkChunks(){
		/* public V get(Object key)
		 * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
		 */
		removeAllArmorStand(); // Remove all armorstand first
		
		HashMap<Location, Integer> Y_Map = new HashMap<Location, Integer>();
		
		List<World> world_list = Bukkit.getWorlds();
		Iterator<World> it = world_list.iterator();
		while(it.hasNext()){
			World world = (World) it.next();
			Chunk[] chunks = world.getLoadedChunks();
			for(Chunk chunk : chunks){
				for(int x=0; x<16; x++){
					for(int z=0; z<16; z++){
						// -1 if no air
						int highest_not_air = -1;
						for(int y=0; y<128; y++){
							if(chunk.getBlock(x, y, z).getType() != Material.AIR){
								highest_not_air = chunk.getBlock(x, y, z).getY();
							}
						}
						// Location's y is always 0
						if(chunk.getBlock(x, highest_not_air, z).getType() == Material.STATIONARY_WATER || chunk.getBlock(x, highest_not_air, z).getType() == Material.WATER)
							Y_Map.put(chunk.getBlock(x, 0, z).getLocation(), 1000); // 1000 means WATER
						else
							Y_Map.put(chunk.getBlock(x, 0, z).getLocation(), highest_not_air);
					}
				}
				// This chunk's work is done
				for(int x=0; x<16; x++){
					for(int z=0; z<16; z++){
						/*
						 * --------x-------->
						 * |
						 * |       Y3
						 * |
						 * z   Y1  Y  Y2
						 * |
						 * |       Y4
						 * |
						 * v
						 * 
						 * null if no this element
						 */
						//System.out.println("Not exists block in chunk =>"+chunk.getBlock(-1, -1, -1).getLocation());
						
						Location not_exist = new Location(Bukkit.getWorld("not_exist"), -1, -1 ,-1); // Never puts this location into hashmap
						Location L_Y = chunk.getBlock(x, 0, z).getLocation().clone();
						Location L_Y1 = chunk.getBlock(x-1, 0, z).getLocation().clone();
						Location L_Y2 = chunk.getBlock(x+1, 0, z).getLocation().clone();
						Location L_Y3 = chunk.getBlock(x, 0, z-1).getLocation().clone();
						Location L_Y4 = chunk.getBlock(x, 0, z+1).getLocation().clone();
						if(x-1 < 0){
							L_Y1 = not_exist;
						}
						if(x+1 > 15){
							L_Y2 = not_exist;
						}
						if(z-1 < 0){
							L_Y3 = not_exist;
						}
						if(z+1 > 15){
							L_Y4 = not_exist;
						}
						
						//System.out.println("L_Y => "+L_Y);
						// below are highest non-AIR
						// if Y == 1000 => this block is water, and need to continue the loop
						Integer Y = Y_Map.get(L_Y);
						
						if(Y == 1000){
							continue;
						}
						
						Integer Y1 = Y_Map.get(L_Y1);
						Integer Y2 = Y_Map.get(L_Y2);
						Integer Y3 = Y_Map.get(L_Y3);
						Integer Y4 = Y_Map.get(L_Y4);
						// Set the same as Y if null => there will be no 落差
						if(Y1 == null)
							Y1=Y;
						if(Y2 == null)
							Y2=Y;
						if(Y3 == null)
							Y3=Y;
						if(Y4 == null)
							Y4=Y;
						// ------------------- change Location's y to the highest non-AIR's y
						L_Y.setY(Y);
						L_Y1.setY(Y1);
						L_Y2.setY(Y2);
						L_Y3.setY(Y3);
						L_Y4.setY(Y4);
						// ----------------reSet loc's x/z to block's x/z
						L_Y.setX(L_Y.getX()+0.5);
						L_Y.setZ(L_Y.getZ()+0.5);
						L_Y1.setX(L_Y1.getX()+0.5);
						L_Y1.setZ(L_Y1.getZ()+0.5);
						L_Y2.setX(L_Y2.getX()+0.5);
						L_Y2.setZ(L_Y2.getZ()+0.5);
						L_Y3.setX(L_Y3.getX()+0.5);
						L_Y3.setZ(L_Y3.getZ()+0.5);
						L_Y4.setX(L_Y4.getX()+0.5);
						L_Y4.setZ(L_Y4.getZ()+0.5);
						// 摔落測試 http://forum.gamer.com.tw/Co.php?bsn=18673&sn=153839
						
						if(Y1-Y >= 13){
							L_Y.setY(L_Y1.getY());
							if(Armor_Map.get(L_Y) == null)
							Armor_Map.put(L_Y, chunk.getWorld().spawn(L_Y, ArmorStand.class));
						}
						if(Y2-Y >= 13){
							L_Y.setY(L_Y2.getY());
							if(Armor_Map.get(L_Y) == null)
							Armor_Map.put(L_Y, chunk.getWorld().spawn(L_Y, ArmorStand.class));
						}
						if(Y3-Y >= 13){
							L_Y.setY(L_Y3.getY());
							if(Armor_Map.get(L_Y) == null)
							Armor_Map.put(L_Y, chunk.getWorld().spawn(L_Y, ArmorStand.class));
						}
						if(Y4-Y >= 13){
							L_Y.setY(L_Y4.getY());
							if(Armor_Map.get(L_Y) == null)
							Armor_Map.put(L_Y, chunk.getWorld().spawn(L_Y, ArmorStand.class));
						}
					}
				}
			}
		}
		// All done, and set the armor's metadata
		Iterator<ArmorStand> ar_it = Armor_Map.values().iterator();
		while(ar_it.hasNext()){
			ArmorStand armorstand = ar_it.next();
			
			//System.out.println("Set armor metadata at => "+armorstand.getLocation());
			
			armorstand.setCustomName("WARN!");
			armorstand.setVisible(false);
			armorstand.setCustomNameVisible(true);
			armorstand.setGravity(false);
			armorstand.setMarker(true);
		}
	}
	
	public static void removeAllArmorStand(){
		Iterator<World> it = Bukkit.getWorlds().iterator();
		while(it.hasNext()){
			World world = it.next();
			Iterator<ArmorStand> ar_it = world.getEntitiesByClass(ArmorStand.class).iterator();
			while(ar_it.hasNext()){
				ArmorStand armorstand = ar_it.next();
				armorstand.remove();
			}
		}
	}
}
