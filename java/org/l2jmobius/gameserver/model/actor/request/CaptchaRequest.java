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
package org.l2jmobius.gameserver.model.actor.request;

import static java.lang.System.currentTimeMillis;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.BotReportTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.captcha.Captcha;

public class CaptchaRequest extends AbstractRequest
{
	private static final byte MAX_ATTEMPTS = 3;
	
	private Captcha _captcha;
	private byte _count = 0;
	private final Instant _timeout;
	
	public CaptchaRequest(Player activeChar, Captcha captcha)
	{
		super(activeChar);
		_captcha = captcha;
		final long currentTime = currentTimeMillis();
		setTimestamp(currentTime);
		scheduleTimeout(Duration.ofMinutes(Config.VALIDATION_TIME).toMillis());
		_timeout = Instant.ofEpochMilli(currentTime).plus(Config.VALIDATION_TIME, ChronoUnit.MINUTES);
	}
	
	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}
	
	public int getRemainingTime()
	{
		return (int) (_timeout.minusMillis(currentTimeMillis()).getEpochSecond());
	}
	
	public void refresh(Captcha captcha)
	{
		_captcha = captcha;
	}
	
	public void newRequest(Captcha captcha)
	{
		_count++;
		_captcha = captcha;
	}
	
	public boolean isLimitReached()
	{
		return _count >= (MAX_ATTEMPTS - 1);
	}
	
	public Captcha getCaptcha()
	{
		return _captcha;
	}
	
	@Override
	public void onTimeout()
	{
		BotReportTable.getInstance().punishBotDueUnsolvedCaptcha(getPlayer());
	}
	
	public int maxAttemps()
	{
		return MAX_ATTEMPTS;
	}
	
	public int remainingAttemps()
	{
		return Math.max(MAX_ATTEMPTS - _count, 0);
	}
}
