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
package handlers.chathandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.PlayerCondOverride;
import org.l2jmobius.gameserver.handler.IChatHandler;
import org.l2jmobius.gameserver.instancemanager.MapRegionManager;
import org.l2jmobius.gameserver.model.BlockList;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Shout chat handler.
 * @author durgus
 */
public class ChatShout implements IChatHandler
{
	private static final ChatType[] CHAT_TYPES =
	{
		ChatType.SHOUT,
	};
	
	@Override
	public void handleChat(ChatType type, Player activeChar, String target, String text, boolean shareLocation)
	{
		if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
		{
			activeChar.sendPacket(SystemMessageId.IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_INCREASE_EVEN_FURTHER_S1_SEC_OF_PROHIBITION_IS_LEFT);
			return;
		}
		if (Config.JAIL_DISABLE_CHAT && activeChar.isJailed() && !activeChar.canOverrideCond(PlayerCondOverride.CHAT_CONDITIONS))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
			return;
		}
		if ((activeChar.getLevel() < Config.MINIMUM_CHAT_LEVEL) && !activeChar.canOverrideCond(PlayerCondOverride.CHAT_CONDITIONS))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CHARACTERS_OF_LV_S1_OR_LOWER_CANNOT_USE_THE_SHOUT_CHAT).addInt(Config.MINIMUM_CHAT_LEVEL));
			return;
		}
		
		if (shareLocation)
		{
			if (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.SHARING_LOCATION_COST)
			{
				activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
				return;
			}
			
			if ((activeChar.getMovieHolder() != null) || activeChar.isFishing() || activeChar.isInInstance() || activeChar.isOnEvent() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || activeChar.isInTraingCamp() || activeChar.isInTimedHuntingZone() || activeChar.isInsideZone(ZoneId.SIEGE))
			{
				activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
				return;
			}
			
			activeChar.destroyItemByItemId("Shared Location", Inventory.LCOIN_ID, Config.SHARING_LOCATION_COST, activeChar, true);
		}
		
		final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text, shareLocation);
		if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.canOverrideCond(PlayerCondOverride.CHAT_CONDITIONS)))
		{
			final int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
			for (Player player : World.getInstance().getPlayers())
			{
				if ((region == MapRegionManager.getInstance().getMapRegionLocId(player)) && !BlockList.isBlocked(player, activeChar) && (player.getInstanceId() == activeChar.getInstanceId()) && !BlockList.isBlocked(activeChar, player))
				{
					if (Config.FACTION_SYSTEM_ENABLED)
					{
						if (Config.FACTION_SPECIFIC_CHAT)
						{
							if ((activeChar.isGood() && player.isGood()) || (activeChar.isEvil() && player.isEvil()))
							{
								player.sendPacket(cs);
							}
						}
						else
						{
							player.sendPacket(cs);
						}
					}
					else
					{
						player.sendPacket(cs);
					}
				}
			}
		}
		else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
		{
			if (!activeChar.canOverrideCond(PlayerCondOverride.CHAT_CONDITIONS) && !activeChar.getClient().getFloodProtectors().canUseGlobalChat())
			{
				activeChar.sendMessage("Do not spam shout channel.");
				return;
			}
			
			for (Player player : World.getInstance().getPlayers())
			{
				if (!BlockList.isBlocked(player, activeChar))
				{
					if (Config.FACTION_SYSTEM_ENABLED)
					{
						if (Config.FACTION_SPECIFIC_CHAT)
						{
							if ((activeChar.isGood() && player.isGood()) || (activeChar.isEvil() && player.isEvil()))
							{
								player.sendPacket(cs);
							}
						}
						else
						{
							player.sendPacket(cs);
						}
					}
					else
					{
						player.sendPacket(cs);
					}
				}
			}
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return CHAT_TYPES;
	}
}