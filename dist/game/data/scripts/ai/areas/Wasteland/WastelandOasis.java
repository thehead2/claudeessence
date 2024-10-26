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
package ai.areas.Wasteland;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Wasteland Oasis AI.
 * @URL https://l2central.info/essence/articles/2491.html
 * @author Mobius
 */
public class WastelandOasis extends AbstractNpcAI
{
	// NPCs
	private static final int OASIS_CREATURE = 22918;
	private static final int VANDER = 22928;
	private static final int ELITE_RAIDER = 22923;
	private static final int ARCHON_OF_DARKNESS = 22924;
	private static final int ASSASSIN_OF_DARKNESS = 22927;
	
	private WastelandOasis()
	{
		addKillId(OASIS_CREATURE, ARCHON_OF_DARKNESS, ASSASSIN_OF_DARKNESS);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (getRandom(10) == 0) // 10%
		{
			if (npc.getId() == OASIS_CREATURE)
			{
				final Npc vander = addSpawn(VANDER, npc);
				final Playable attacker = isSummon ? killer.getServitors().values().stream().findFirst().orElse(killer.getPet()) : killer;
				addAttackPlayerDesire(vander, attacker);
				npc.deleteMe();
			}
			else
			{
				final Npc eliteRaider = addSpawn(ELITE_RAIDER, npc);
				final Playable attacker = isSummon ? killer.getServitors().values().stream().findFirst().orElse(killer.getPet()) : killer;
				addAttackPlayerDesire(eliteRaider, attacker);
				npc.deleteMe();
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new WastelandOasis();
	}
}
