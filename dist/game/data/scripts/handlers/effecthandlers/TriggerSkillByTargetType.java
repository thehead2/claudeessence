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
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillCaster;

/**
 * @author Liamxroy
 */
public class TriggerSkillByTargetType extends AbstractEffect
{
	private final SkillHolder _allySkill;
	private final SkillHolder _enemySkill;
	
	public TriggerSkillByTargetType(StatSet params)
	{
		// Just use closeSkill and rangeSkill parameters.
		_allySkill = new SkillHolder(params.getInt("allySkill"), params.getInt("allySkillLevel", 1));
		_enemySkill = new SkillHolder(params.getInt("enemySkill"), params.getInt("enemySkillLevel", 1));
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if ((effected == null) || !effector.isPlayer())
		{
			return;
		}
		
		final Skill triggerSkill = effected.isAutoAttackable(effector) ? _enemySkill.getSkill() : _allySkill.getSkill();
		if (triggerSkill == null)
		{
			return;
		}
		
		SkillCaster.triggerCast(effector, effected, triggerSkill);
	}
}