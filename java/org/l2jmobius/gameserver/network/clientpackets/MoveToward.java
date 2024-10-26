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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author Mobius
 */
public class MoveToward extends ClientPacket
{
	private int _heading;
	
	@Override
	protected void readImpl()
	{
		_heading = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		if (!Config.ENABLE_KEYBOARD_MOVEMENT)
		{
			return;
		}
		
		final Player player = getPlayer();
		if ((player == null) || player.isControlBlocked())
		{
			return;
		}
		
		final double angle = Util.convertHeadingToDegree(_heading);
		final double radian = Math.toRadians(angle);
		final double course = Math.toRadians(180);
		final double frontDistance = player.getMoveSpeed();
		final int x1 = (int) (Math.cos(Math.PI + radian + course) * frontDistance);
		final int y1 = (int) (Math.sin(Math.PI + radian + course) * frontDistance);
		final int x = player.getX() + x1;
		final int y = player.getY() + y1;
		final Location destination = GeoEngine.getInstance().getValidLocation(player.getX(), player.getY(), player.getZ(), x, y, player.getZ(), player.getInstanceWorld());
		
		player.setCursorKeyMovement(true);
		player.setLastServerPosition(player.getX(), player.getY(), player.getZ());
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, destination);
		
		// Remove queued skill upon move request.
		if (player.getQueuedSkill() != null)
		{
			player.setQueuedSkill(null, null, false, false);
		}
		
		// Mobius: Check spawn protections.
		player.onActionRequest();
	}
}
