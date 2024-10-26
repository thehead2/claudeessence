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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.WritableBuffer;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @author Meliodas
 */
public class ExDamagePopUp extends ServerPacket
{
	public static final byte NOMAKE = 0;
	public static final byte NORMAL_ATTACK = 1;
	public static final byte CONSECUTIVE_ATTACK = 2;
	public static final byte CRITICAL = 3;
	public static final byte OVERHIT = 4;
	public static final byte RECOVER_HP = 5;
	public static final byte RECOVER_MP = 6;
	public static final byte GET_SP = 7;
	public static final byte GET_EXP = 8;
	public static final byte MAGIC_DEFIANCE = 9;
	public static final byte SHIELD_GUARD = 10;
	public static final byte DODGE = 11;
	public static final byte IMMUNE = 12;
	public static final byte SKILL_HIT = 13;
	
	private final int _caster;
	private final int _target;
	private final int _damage;
	private final byte _type;
	
	public ExDamagePopUp(int caster, int target, int damage, byte type)
	{
		_caster = caster;
		_target = target;
		_damage = damage;
		_type = type;
	}
	
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DAMAGE_POPUP.writeId(this, buffer);
		buffer.writeInt(_caster);
		buffer.writeInt(_target);
		buffer.writeInt(-_damage);
		buffer.writeByte(_type);
	}
}