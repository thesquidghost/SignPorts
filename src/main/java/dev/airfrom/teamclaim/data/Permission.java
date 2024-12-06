package dev.airfrom.teamclaim.data;

public enum Permission {
	
	TEAM_INVITE_PLAYERS("team_invite_players", false, true, null),
	TEAM_MANAGE_CLAIMS("team_manage_claims", false, true, null),
	TEAM_MANAGE_CLAIM_SPAWNS("team_manage_claim_spawns", false, true, null),
	TEAM_DELETE_CLAIMS("team_delete_claims", false, true, null),
	TEAM_WITHDRAW_CLAIM_BLOCKS("team_withdraw_claim_blocks", false, true, null),
	TEAM_MANAGE_INVITES("team_manage_invites", false, true, null),
	TEAM_CREATE_INVITES("team_create_invites", false, true, null),
	TEAM_PUBLIC_ACCESS("team_public_access", true, true, null),
	
	CLAIM_ACCESS("claim_access", true, false, null),
	CLAIM_PLACE_BREAK_BLOCK("claim_place_break_block", true, false, null),
	CLAIM_INTERACT("claim_interact", true, false, null),
	
	CLAIM_MANAGE_PUBLIC_ACCESS("claim_manage_public_access", false, false, Property.PUBLIC_ACCESS),
	CLAIM_MANAGE_ENTER_EXIT_MSG("claim_manage_enter_exit_msg", false, false, null),
	CLAIM_MANAGE_FALL_DAMAGE("claim_manage_fall_damage", false, false, Property.FALL_DAMAGE),
	CLAIM_MANAGE_FIRE_SPREADING("claim_manage_fire_spreading", false, false, Property.FIRE_SPREADING),
	CLAIM_MANAGE_EXPLOSIONS("claim_manage_explosions", false, false, Property.EXPLOSIONS),
	CLAIM_MANAGE_MOB_SPAWNING("claim_manage_mob_spawning", false, false, Property.MOB_SPAWNING),
	CLAIM_MANAGE_TEAM_PVP("claim_manage_team_pvp", false, false, Property.TEAM_PVP),
	CLAIM_MANAGE_GLOBAL_PVP("claim_manage_global_pvp", false, false, Property.GLOBAL_PVP),
	CLAIM_MANAGE_HURT_ANIMAL("claim_manage_hurt_animal", false, false, Property.HURT_ANIMAL),
	CLAIM_MANAGE_HURT_MONSTER("claim_manage_hurt_monster", false, false, Property.HURT_MONSTER);

	private String name;
	private boolean isGlobal;
	private boolean isTeamPermission;
	private Property property;
	
	Permission(String name, boolean isGlobal, boolean isTeamPermission, Property property){
		this.name = name;
		this.isGlobal = isGlobal;
		this.isTeamPermission = isTeamPermission;
		this.property = property;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isGlobalPermission(){
		return isGlobal;
	}
	
	public boolean isTeamPermission(){
		return isTeamPermission;
	}
	
	public Property getLinkedProperty() {
		return property;
	}
	
	public static Permission getPermission(String s) {
		for(Permission perm : Permission.values()) {
			if(s.equals(perm.getName())) return perm;
		}
		return null;
	}
	
	

}
