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
package handlers.effecthandlers;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExPCCafePointInfo;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Increase PcCafe points permanently.
 * @author `NasSeKa`
 */
public class AddPcCafePoints extends AbstractEffect
{
	private final int _amount;
	
	public AddPcCafePoints(StatSet params)
	{
		_amount = params.getInt("amount", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effected.isPlayer())
		{
			return;
		}
		
		final Player player = effected.getActingPlayer();
		final int currentPoints = player.getPcCafePoints();
		final int upgradePoints = currentPoints + _amount;
		player.setPcCafePoints(upgradePoints);
		final SystemMessage message = new SystemMessage(SystemMessageId.YOU_HAVE_RECEIVED_S1_POINT_S_AS_A_DAILY_REWARD_FOR_USING_EVA_S_GRACE).addInt(_amount);
		player.sendPacket(message);
		player.sendPacket(new ExPCCafePointInfo(currentPoints, upgradePoints, 1));
	}
}
