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
package org.l2jmobius.gameserver.network.serverpackets.worldexchange;

import java.util.List;
import java.util.Map;

import org.l2jmobius.commons.network.WritableBuffer;
import org.l2jmobius.gameserver.enums.AttributeType;
import org.l2jmobius.gameserver.enums.WorldExchangeItemStatusType;
import org.l2jmobius.gameserver.instancemanager.WorldExchangeManager;
import org.l2jmobius.gameserver.model.VariationInstance;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.ensoul.EnsoulOption;
import org.l2jmobius.gameserver.model.holders.WorldExchangeHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;
import org.l2jmobius.gameserver.network.serverpackets.ServerPacket;

/**
 * @author Index
 */
public class WorldExchangeSettleList extends ServerPacket
{
	private final Player _player;
	
	public WorldExchangeSettleList(Player player)
	{
		_player = player;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLD_EXCHANGE_SETTLE_LIST.writeId(this, buffer);
		
		final Map<WorldExchangeItemStatusType, List<WorldExchangeHolder>> holders = WorldExchangeManager.getInstance().getPlayerBids(_player.getObjectId());
		if (holders.isEmpty())
		{
			buffer.writeInt(0); // RegiItemDataList
			buffer.writeInt(0); // RecvItemDataList
			buffer.writeInt(0); // TimeOutItemDataList
			return;
		}
		
		buffer.writeInt(holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED).size());
		for (WorldExchangeHolder holder : holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED))
		{
			getItemInfo(buffer, holder);
		}
		
		buffer.writeInt(holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD).size());
		for (WorldExchangeHolder holder : holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD))
		{
			getItemInfo(buffer, holder);
		}
		
		buffer.writeInt(holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME).size());
		for (WorldExchangeHolder holder : holders.get(WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME))
		{
			getItemInfo(buffer, holder);
		}
	}
	
	private void getItemInfo(WritableBuffer buffer, WorldExchangeHolder holder)
	{
		buffer.writeLong(holder.getWorldExchangeId());
		buffer.writeLong(holder.getPrice());
		buffer.writeInt((int) (holder.getEndTime() / 1000L));
		Item item = holder.getItemInstance();
		buffer.writeInt(item.getId());
		buffer.writeLong(item.getCount());
		buffer.writeInt(item.getEnchantLevel() < 1 ? 0 : item.getEnchantLevel());
		VariationInstance iv = item.getAugmentation();
		buffer.writeInt(iv != null ? iv.getOption1Id() : 0);
		buffer.writeInt(iv != null ? iv.getOption2Id() : 0);
		buffer.writeInt(-1); // IntensiveItemClassID
		buffer.writeShort(item.getAttackAttribute() != null ? item.getAttackAttribute().getType().getClientId() : 0);
		buffer.writeShort(item.getAttackAttribute() != null ? item.getAttackAttribute().getValue() : 0);
		buffer.writeShort(item.getDefenceAttribute(AttributeType.FIRE));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.WATER));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.WIND));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.EARTH));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.HOLY));
		buffer.writeShort(item.getDefenceAttribute(AttributeType.DARK));
		buffer.writeInt(item.getVisualId());
		
		final List<EnsoulOption> soul = (List<EnsoulOption>) holder.getItemInfo().getSoulCrystalOptions();
		try
		{
			buffer.writeInt(soul.get(0).getId());
		}
		catch (IndexOutOfBoundsException ignored)
		{
			buffer.writeInt(0);
		}
		
		try
		{
			buffer.writeInt(soul.get(1).getId());
		}
		catch (IndexOutOfBoundsException ignored)
		{
			buffer.writeInt(0);
		}
		
		final List<EnsoulOption> specialSoul = (List<EnsoulOption>) holder.getItemInfo().getSoulCrystalSpecialOptions();
		try
		{
			buffer.writeInt(specialSoul.get(0).getId());
		}
		catch (IndexOutOfBoundsException ignored)
		{
			buffer.writeInt(0);
		}
		
		buffer.writeShort(item.isBlessed());
	}
}
