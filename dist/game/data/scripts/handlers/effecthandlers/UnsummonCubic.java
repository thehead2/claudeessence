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
import org.l2jmobius.gameserver.model.cubic.Cubic;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.ExUserInfoCubic;

public class UnsummonCubic extends AbstractEffect
{
	private final int _cubicId;
	
	public UnsummonCubic(StatSet params)
	{
		_cubicId = params.getInt("cubicId", -1);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		final Player player = effector.getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_cubicId < 0)
		{
			for (Cubic cubic : player.getCubics().values())
			{
				cubic.deactivate();
			}
			player.sendPacket(new ExUserInfoCubic(player));
			player.broadcastCharInfo();
		}
		else
		{
			final Cubic cubic = player.getCubicById(_cubicId);
			if (cubic != null)
			{
				cubic.deactivate();
				player.sendPacket(new ExUserInfoCubic(player));
				player.broadcastCharInfo();
			}
		}
	}
}
