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
package ai.areas.Giran;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.OnDayNightChange;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;

/**
 * @author Mobius
 */
public class GiranZoneEffect extends Quest
{
	private static final ZoneType GIRAN_ZONE = ZoneManager.getInstance().getZoneById(11020);
	private static boolean _isNight = GameTimeTaskManager.getInstance().isNight();
	
	private GiranZoneEffect()
	{
		super(-1);
		addEnterZoneId(GIRAN_ZONE.getId());
	}
	
	@Override
	public String onEnterZone(Creature creature, ZoneType zone)
	{
		sendEffect(creature);
		return super.onEnterZone(creature, zone);
	}
	
	@RegisterEvent(EventType.ON_DAY_NIGHT_CHANGE)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void onDayNightChange(OnDayNightChange event)
	{
		_isNight = event.isNight();
		for (Creature creature : GIRAN_ZONE.getCharactersInside())
		{
			sendEffect(creature);
		}
	}
	
	private void sendEffect(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.sendPacket(_isNight ? Config.GIRAN_NIGHT_EFFECT : Config.GIRAN_DAY_EFFECT);
		}
	}
	
	public static void main(String[] args)
	{
		new GiranZoneEffect();
	}
}
