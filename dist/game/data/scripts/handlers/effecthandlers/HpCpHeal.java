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

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ShotType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectFlag;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.CrystalType;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.Formulas;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExMagicAttackInfo;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * HpCpHeal effect implementation.
 * @author Sdw, Mobius
 */
public class HpCpHeal extends AbstractEffect
{
	private final double _power;
	
	public HpCpHeal(StatSet params)
	{
		_power = params.getDouble("power", 0);
		
		if (params.contains("amount"))
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + " should use power instead of amount.");
		}
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.HEAL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (effected.isDead() || effected.isDoor() || effected.isHpBlocked())
		{
			return;
		}
		
		if ((effected != effector) && effected.isAffected(EffectFlag.FACEOFF))
		{
			return;
		}
		
		double amount = _power;
		double staticShotBonus = 0;
		double mAtkMul = 1;
		final boolean sps = skill.isMagic() && effector.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = skill.isMagic() && effector.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final double shotsBonus = effector.getStat().getValue(Stat.SHOTS_BONUS);
		
		if (((sps || bss) && (effector.isPlayer() && effector.getActingPlayer().isMageClass())) || effector.isSummon())
		{
			staticShotBonus = skill.getMpConsume(); // static bonus for spiritshots
			mAtkMul = bss ? 4 * shotsBonus : 2 * shotsBonus;
			staticShotBonus *= bss ? 2.4 : 1.0;
		}
		else if ((sps || bss) && effector.isNpc())
		{
			staticShotBonus = 2.4 * skill.getMpConsume(); // always blessed spiritshots
			mAtkMul = 4 * shotsBonus;
		}
		else
		{
			// no static bonus
			// grade dynamic bonus
			final Item weaponInst = effector.getActiveWeaponInstance();
			if (weaponInst != null)
			{
				mAtkMul = weaponInst.getTemplate().getCrystalType() == CrystalType.S84 ? 4 : weaponInst.getTemplate().getCrystalType() == CrystalType.S80 ? 2 : 1;
			}
			// shot dynamic bonus
			mAtkMul = bss ? mAtkMul * 4 : mAtkMul + 1;
		}
		
		if (!skill.isStatic())
		{
			amount += staticShotBonus + Math.sqrt(mAtkMul * effector.getMAtk());
			amount *= effected.getStat().getValue(Stat.HEAL_EFFECT, 1);
			amount += effected.getStat().getValue(Stat.HEAL_EFFECT_ADD, 0);
			amount *= (item == null) && effector.isPlayable() ? Config.PLAYER_HEALING_SKILL_MULTIPLIERS[effector.getActingPlayer().getClassId().getId()] : 1f;
			// Heal critic, since CT2.3 Gracia Final
			if (skill.isMagic() && (Formulas.calcCrit(skill.getMagicCriticalRate(), effector, effected, skill) || effector.isAffected(EffectFlag.HPCPHEAL_CRITICAL)))
			{
				amount *= 3;
				effector.sendPacket(SystemMessageId.M_CRITICAL);
				effector.sendPacket(new ExMagicAttackInfo(effector.getObjectId(), effected.getObjectId(), ExMagicAttackInfo.CRITICAL_HEAL));
				if (effected.isPlayer() && (effected != effector))
				{
					effected.sendPacket(new ExMagicAttackInfo(effector.getObjectId(), effected.getObjectId(), ExMagicAttackInfo.CRITICAL_HEAL));
				}
			}
		}
		
		// Additional potion HP.
		double additionalHp = 0;
		double additionalHpPer = 1;
		
		// Additional potion CP.
		double additionalCp = 0;
		double additionalCpPer = 1;
		
		final boolean isPotion = (item != null) && (item.isPotion() || item.isElixir());
		if (isPotion)
		{
			additionalHp = effected.getStat().getValue(Stat.ADDITIONAL_POTION_HP, 0);
			additionalCp = effected.getStat().getValue(Stat.ADDITIONAL_POTION_CP, 0);
			
			additionalHpPer = ((effected.getStat().getValue(Stat.ADDITIONAL_POTION_HP_PER, 0) / 100) + 1);
			additionalCpPer = ((effected.getStat().getValue(Stat.ADDITIONAL_POTION_CP_PER, 0) / 100) + 1);
			
			// Classic Potion Mastery
			// TODO: Create an effect if more mastery skills are added.
			amount *= 1 + (effected.getAffectedSkillLevel(CommonSkill.POTION_MASTERY.getId()) / 100);
		}
		
		// Prevents overheal and negative amount
		final double healAmount = Math.max(Math.min(amount * (isPotion ? additionalHpPer : 1), effected.getMaxRecoverableHp() - effected.getCurrentHp()), 0);
		if (healAmount != 0)
		{
			final double newHp = healAmount + effected.getCurrentHp();
			if ((newHp + additionalHp) > effected.getMaxRecoverableHp())
			{
				additionalHp = Math.max(effected.getMaxRecoverableHp() - newHp, 0);
			}
			effected.setCurrentHp(newHp + additionalHp, false);
			
			if (effector.isPlayer() && (effector != effected))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_HAS_BEEN_RECOVERED_BY_C1);
				sm.addString(effector.getName());
				sm.addInt((int) (healAmount + additionalHp));
				effected.sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_VE_RECOVERED_S1_HP);
				sm.addInt((int) (healAmount + additionalHp));
				effected.sendPacket(sm);
			}
		}
		
		// CP recovery.
		if (effected.isPlayer())
		{
			amount = Math.max(Math.min((amount - healAmount) * (isPotion ? additionalCpPer : 1), effected.getMaxRecoverableCp() - effected.getCurrentCp()), 0);
			if (amount != 0)
			{
				final double newCp = amount + effected.getCurrentCp();
				if ((newCp + additionalCp) > effected.getMaxRecoverableCp())
				{
					additionalCp = Math.max(effected.getMaxRecoverableCp() - newCp, 0);
				}
				effected.setCurrentCp(newCp + additionalCp, false);
				
				if (effector != effected)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOVERED_S2_CP_WITH_C1_S_HELP);
					sm.addString(effector.getName());
					sm.addInt((int) (amount + additionalCp));
					effected.sendPacket(sm);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOVERED_S1_CP);
					sm.addInt((int) (amount + additionalCp));
					effected.sendPacket(sm);
				}
			}
		}
		
		effected.broadcastStatusUpdate(effector);
	}
}