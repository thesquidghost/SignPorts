package dev.airfrom.teamclaim.data;

import org.bukkit.Material;

public enum Property {
	
	PUBLIC_ACCESS("public_access", Material.IRON_DOOR),
	FALL_DAMAGE("fall_damage", Material.IRON_BOOTS),
	EXPLOSIONS("explosions", Material.TNT),
	TEAM_PVP("team_pvp", Material.IRON_SWORD),
	GLOBAL_PVP("global_pvp", Material.DIAMOND_SWORD),
	FIRE_SPREADING("fire_spreading", Material.CAMPFIRE),
	MOB_SPAWNING("mob_spawning", Material.SPAWNER),
	HURT_ANIMAL("hurt_animal", Material.LEATHER),
	HURT_MONSTER("hurt_monster", Material.ZOMBIE_HEAD),;

	private String name;
	private Material icon;
	
	Property(String name, Material icon){
		this.name = name;
		this.icon = icon;
	}
	
	public String getName(){
		return name;
	}
	
	public Material getIcon(){
		return icon;
	}
	
	public static Property getProperty(String s) {
		for(Property prop : Property.values()) {
			if(s.equals(prop.getName())) return prop;
		}
		return null;
	}
	/*
	public static Grade getGrade(Player p){
		Fichier player_file = new Fichier(p);
		return player_file.getGrade();
	}
	 */

}
