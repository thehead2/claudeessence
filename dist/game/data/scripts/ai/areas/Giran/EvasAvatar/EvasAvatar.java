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
package ai.areas.Giran.EvasAvatar;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skill.SkillCaster;

import ai.AbstractNpcAI;

/**
 * @author Liamxroy
 */
public class EvasAvatar extends AbstractNpcAI
{
	// NPC
	private static final int EVAS_AVATAR = 34455;
	// Skills
	private static final SkillHolder[] BLESSING_OF_WATER =
	{
		new SkillHolder(48934, 1),
		new SkillHolder(48934, 2),
		new SkillHolder(48934, 3),
		new SkillHolder(48934, 4),
		new SkillHolder(48934, 5),
		new SkillHolder(48934, 6),
	};
	// Items
	private static final int WONDROUS = 100884; // Wondrous Water of Life (Time-limited) Sealed
	private static final int TREE_LEAF = 100886; // Young Mother Tree's Leaf (Time-limited) Sealed
	// Misc
	private static final double TREE_LEAF_PROBABILITY = 5;
	
	private EvasAvatar()
	{
		addStartNpc(EVAS_AVATAR);
		addFirstTalkId(EVAS_AVATAR);
		addTalkId(EVAS_AVATAR);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		if (event.equals("giveBuff"))
		{
			if (!hasQuestItems(player, WONDROUS))
			{
				return "34455-01.html";
			}
			
			takeItems(player, WONDROUS, 1);
			
			SkillCaster.triggerCast(npc, player, getBuffForDay().getSkill());
			
			// Buff players around
			World.getInstance().forEachVisibleObjectInRange(npc, Player.class, 500, target ->
			{
				if ((target != player) && (target != null) && !target.isDead() && GeoEngine.getInstance().canSeeTarget(npc, target))
				{
					getBuffForDay().getSkill().applyEffects(npc, target);
				}
			});
			
			// Check probability for receiving Young Mother Tree's Leaf.
			if (getRandom(100) < TREE_LEAF_PROBABILITY)
			{
				giveItems(player, TREE_LEAF, 1);
			}
		}
		return null;
	}
	
	private SkillHolder getBuffForDay()
	{
		final GregorianCalendar date = new GregorianCalendar();
		final int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		switch (dayOfWeek)
		{
			case Calendar.MONDAY:
			{
				return BLESSING_OF_WATER[0];
			}
			case Calendar.TUESDAY:
			{
				return BLESSING_OF_WATER[1];
			}
			case Calendar.WEDNESDAY:
			{
				return BLESSING_OF_WATER[2];
			}
			case Calendar.THURSDAY:
			{
				return BLESSING_OF_WATER[3];
			}
			case Calendar.FRIDAY:
			{
				return BLESSING_OF_WATER[4];
			}
			case Calendar.SATURDAY:
			case Calendar.SUNDAY:
			{
				return BLESSING_OF_WATER[5];
			}
			default:
			{
				return BLESSING_OF_WATER[0];
			}
		}
	}
	
	public static void main(String[] args)
	{
		new EvasAvatar();
	}
}