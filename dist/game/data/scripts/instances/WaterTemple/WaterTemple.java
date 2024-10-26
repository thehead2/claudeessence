/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package instances.WaterTemple;

import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.Earthquake;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.OnEventTrigger;

import instances.AbstractInstance;

/**
 * @author Serenitty
 */
public class WaterTemple extends AbstractInstance
{
	// NPCs
	private static final int WATER_SLIME = 19109;
	private static final int TROOP_CHIEFT = 19108;
	
	private static final int BREATH_WATER = 19111;
	private static final int UNDINE = 19110;
	private static final int EROSION_PRIEST = 19114;
	
	private static final int TEMPLE_GUARDIAN_WARRIOR = 19112;
	private static final int TEMPLE_GUARDIAN_WIZARD = 19113;
	
	private static final int ABER = 19115; // RaidBoss
	private static final int ELLE = 19116; // RaidBoss
	
	private static final int AOS = 34464;
	private static final int ANIDA = 34463;
	
	private static final int FLOOD_CHANCE = 5;
	private static final int PLUS_BOSS_CHANCE = 20;
	
	private static final int FLOOD_STAGE_1 = 22250130;
	private static final int FLOOD_STAGE_2 = 22250132;
	private static final int FLOOD_STAGE_3 = 22250134;
	
	private static final int FLOOD_STAGE_FINAL_FLOOR_1 = 22250144;
	private static final int FLOOD_STAGE_FINAL_FLOOR_2 = 22250146;
	
	// Teleports
	private static final Location TELEPORT_STAGE_2 = new Location(71101, 242498, -8425);
	private static final Location TELEPORT_STAGE_3 = new Location(72944, 242782, -7651);
	private static final Location TELEPORT_OUTSIDE = new Location(83763, 147184, -3404);
	
	// Zones
	private static final ZoneType WATER_ZONE_1 = ZoneManager.getInstance().getZoneByName("Water Temple 1");
	private static final ZoneType WATER_ZONE_2 = ZoneManager.getInstance().getZoneByName("Water Temple 2");
	private static final ZoneType WATER_ZONE_3 = ZoneManager.getInstance().getZoneByName("Water Temple 3");
	
	// Misc
	private static final int TEMPLATE_ID = 2008;
	
	public WaterTemple()
	{
		super(TEMPLATE_ID);
		addInstanceEnterId(TEMPLATE_ID);
		addInstanceLeaveId(TEMPLATE_ID);
		addInstanceCreatedId(TEMPLATE_ID);
		addSpawnId(EROSION_PRIEST);
		addKillId(WATER_SLIME, TROOP_CHIEFT);
		addKillId(BREATH_WATER, UNDINE);
		addKillId(TEMPLE_GUARDIAN_WARRIOR, TEMPLE_GUARDIAN_WIZARD);
		addKillId(ABER, ELLE);
		addAttackId(ABER, ELLE);
		addFirstTalkId(AOS, ANIDA);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "ENTER":
			{
				if (player.isInParty())
				{
					final Party party = player.getParty();
					final boolean isInCC = party.isInCommandChannel();
					final List<Player> members = isInCC ? party.getCommandChannel().getMembers() : party.getMembers();
					for (Player member : members)
					{
						if (!member.isInsideRadius3D(npc, 1000))
						{
							player.sendMessage("Player " + member.getName() + " must go closer to Adella.");
						}
						enterInstance(member, npc, TEMPLATE_ID);
					}
				}
				else if (player.isGM())
				{
					enterInstance(player, npc, TEMPLATE_ID);
					player.sendMessage("SYS: You have entered as GM/Admin to Water Temple.");
				}
				else
				{
					if (!player.isInsideRadius3D(npc, 1000))
					{
						player.sendMessage("You must go closer to Jio.");
					}
					enterInstance(player, npc, TEMPLATE_ID);
				}
				break;
			}
			
			case "RESUME_POSITION":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					positionCheck(player, world);
				}
				break;
			}
			
			case "EROSION_SPAWN":
			{
				final Instance world = npc.getInstanceWorld();
				if ((world != null) && (world.getNpc(EROSION_PRIEST) == null))
				{
					world.spawnGroup("ErosionPriest");
					startQuestTimer("EROSION_SPAWN", 80000, npc, null);
				}
				break;
			}
			case "START_SPAWN":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					world.getPlayers().forEach(p -> p.sendPacket(new ExSendUIEvent(p, false, false, (int) (world.getRemainingTime() / 1000), 0, NpcStringId.WATER_TEMPLE_S_REMAINING_TIME)));
					world.broadcastPacket(new ExShowScreenMessage(NpcStringId.PLEASE_KILL_THE_MONSTERS_THAT_THREATEN_OUR_MOTHER_TREE, 2, 10000, true));
					world.spawnGroup("Stage1");
					if (world.getNpc(AOS) != null)
					{
						npc.deleteMe();
					}
				}
				break;
			}
			case "FINAL_BOSS":
			{
				final Instance world = npc.getInstanceWorld();
				if (world != null)
				{
					int randomChance = Rnd.get(100);
					if (randomChance < PLUS_BOSS_CHANCE)
					{
						world.broadcastPacket(new ExShowScreenMessage(NpcStringId.GODDESS_OF_WATER_ELLE_APPEARS, 2, 10000, true));
						
						addSpawn(ELLE, 71796, 242611, -6918, 16145, false, 0, false, world.getId());
					}
					else
					{
						world.broadcastPacket(new ExShowScreenMessage(NpcStringId.EVA_S_FOLLOWER_ABER_APPEARS, 2, 10000, true));
						
						addSpawn(ABER, 71796, 242611, -6918, 16145, false, 0, false, world.getId());
					}
					world.broadcastPacket(new OnEventTrigger(FLOOD_STAGE_FINAL_FLOOR_1, true));
					world.broadcastPacket(new OnEventTrigger(FLOOD_STAGE_FINAL_FLOOR_2, true));
					startQuestTimer("EROSION_SPAWN", 50000, npc, null);
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public void onInstanceEnter(Player player, Instance world)
	{
		final int currentStage = world.getParameters().getInt("stage", 0);
		world.getParameters().set("isOutside", false);
		switch (currentStage)
		{
			case 2:
			{
				player.sendPacket(new OnEventTrigger(FLOOD_STAGE_1, true));
				break;
			}
			case 3:
			{
				player.sendPacket(new OnEventTrigger(FLOOD_STAGE_2, true));
				break;
			}
			case 4:
			{
				player.sendPacket(new OnEventTrigger(FLOOD_STAGE_3, true));
				player.sendPacket(new OnEventTrigger(FLOOD_STAGE_FINAL_FLOOR_1, true));
				player.sendPacket(new OnEventTrigger(FLOOD_STAGE_FINAL_FLOOR_2, true));
				break;
			}
		}
		sendResumeUi(player, world);
	}
	
	@Override
	public void onInstanceCreated(Instance world, Player player)
	{
		world.getParameters().set("stage", 1);
		
		final Npc aosNpc = addSpawn(AOS, 69000, 243368, -8734, 0, false, 0, false, world.getId());
		aosNpc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.PLEASE_KILL_THE_MONSTERS_THAT_ARE_TRYING_TO_STEAL_THE_WATER_ENERGY_AND_HELP_ME_CLEAR_THIS_PLACE);
		aosNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(70441, 243470, -9179));
		
		startQuestTimer("START_SPAWN", 10000, aosNpc, null);
	}
	
	@Override
	public void onInstanceLeave(Player player, Instance instance)
	{
		player.sendPacket(new ExSendUIEvent(player, false, false, 0, 0, NpcStringId.WATER_TEMPLE_S_REMAINING_TIME));
		instance.getParameters().set("isOutside", true);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final Instance world = npc.getInstanceWorld();
		if ((attacker != null) && (world != null))
		{
			final int playerId = attacker.getObjectId();
			final String playerKey = "attackers_" + playerId;
			final boolean didAttack = world.getParameters().getBoolean(playerKey, false);
			if (!didAttack)
			{
				world.getParameters().set(playerKey, true);
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (world == null)
		{
			return null;
		}
		
		int currentStage = world.getParameters().getInt("stage", 0);
		switch (npc.getId())
		{
			case TROOP_CHIEFT:
			case WATER_SLIME:
			{
				handleStage(world, currentStage, 1, FLOOD_STAGE_1, "Stage1", "Stage2", NpcStringId.TO_THE_UPPER_LEVEL_WHERE_MONSTERS_SEEKING_THE_WATER_SPIRIT_S_POWER_DWELL);
				WATER_ZONE_1.setEnabled(true, world.getId());
				break;
			}
			case BREATH_WATER:
			case UNDINE:
			{
				handleStage(world, currentStage, 2, FLOOD_STAGE_2, "Stage2", "Stage3", NpcStringId.WHERE_MONSTERS_REVEAL_THEIR_TRUE_FACES);
				WATER_ZONE_2.setEnabled(true, world.getId());
				break;
			}
			case TEMPLE_GUARDIAN_WARRIOR:
			case TEMPLE_GUARDIAN_WIZARD:
			{
				if ((Rnd.get(100) < FLOOD_CHANCE) && (currentStage == 3))
				{
					world.broadcastPacket(new OnEventTrigger(FLOOD_STAGE_3, true));
					WATER_ZONE_3.setEnabled(true, world.getId());
					world.getParameters().set("stage", currentStage + 1);
					world.despawnGroup("Stage3");
					sendEarthquake(world);
					startQuestTimer("FINAL_BOSS", 4000, npc, killer);
				}
				break;
			}
			case ABER:
			case ELLE:
			{
				rewardPlayersForBossKill(npc, world);
				world.finishInstance();
				addSpawn(ANIDA, 71242, 242569, -6922, 5826, false, 0, false, world.getId());
				break;
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		final Instance world = npc.getInstanceWorld();
		if (world != null)
		{
			for (Player player : world.getPlayers())
			{
				addAttackPlayerDesire(npc, player);
			}
		}
		return super.onSpawn(npc);
	}
	
	private void handleStage(Instance world, int currentStage, int requiredStage, int floodStage, String despawnGroup, String spawnGroup, NpcStringId message)
	{
		if ((Rnd.get(100) < FLOOD_CHANCE) && (currentStage == requiredStage))
		{
			world.broadcastPacket(new OnEventTrigger(floodStage, true));
			if (message != null)
			{
				world.broadcastPacket(new ExShowScreenMessage(message, ExShowScreenMessage.BOTTOM_RIGHT, 10000, false));
			}
			world.getParameters().set("stage", currentStage + 1);
			if (despawnGroup != null)
			{
				world.despawnGroup(despawnGroup);
			}
			if (spawnGroup != null)
			{
				world.spawnGroup(spawnGroup);
			}
			sendEarthquake(world);
		}
	}
	
	private void sendEarthquake(Instance world)
	{
		for (Player player : world.getPlayers())
		{
			player.sendPacket(new Earthquake(player, 15, 5));
		}
	}
	
	private void rewardPlayersForBossKill(Npc npc, Instance world)
	{
		final int finalReward = (npc.getId() == ABER) ? 101259 : 101260;
		for (Player player : world.getPlayers())
		{
			if ((player != null) && player.isOnline())
			{
				final int playerId = player.getObjectId();
				final String playerKey = "attackers_" + playerId;
				final boolean didAttack = world.getParameters().getBoolean(playerKey, false);
				if (didAttack)
				{
					player.addItem("reward", finalReward, 1, player, true);
					world.getParameters().set(playerKey, false);
				}
			}
		}
	}
	
	private void sendResumeUi(Player player, Instance world)
	{
		final boolean isOutside = world.getParameters().getBoolean("isOutside", false);
		if (isOutside)
		{
			sendResumeUi(player, world);
			world.getParameters().set("isOutside", false);
			player.sendPacket(new ExSendUIEvent(player, false, false, (int) (world.getRemainingTime() / 1000), 0, NpcStringId.WATER_TEMPLE_S_REMAINING_TIME));
			if (world.getNpc(AOS) != null)
			{
				addSpawn(AOS, 69000, 243368, -8734, 0, false, 0, false, world.getId());
			}
		}
	}
	
	public void positionCheck(Player player, Instance world)
	{
		final int currentStage = world.getParameters().getInt("stage", 0);
		switch (currentStage)
		{
			case 1:
			case 2:
			{
				player.teleToLocation(TELEPORT_STAGE_2);
				break;
			}
			case 3:
			{
				player.teleToLocation(TELEPORT_STAGE_3);
				break;
			}
			default:
			{
				player.teleToLocation(TELEPORT_OUTSIDE, null);
				break;
			}
		}
	}
	
	public static void main(String[] args)
	{
		new WaterTemple();
	}
}
