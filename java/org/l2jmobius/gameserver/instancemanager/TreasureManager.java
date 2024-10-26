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
package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.siege.Castle;

public class TreasureManager
{
	private static final Logger LOGGER = Logger.getLogger(TreasureManager.class.getName());
	
	private static final String TREASURE_MANAGER_NEXT_RUN_VAR = "TREASURE_MANAGER_NEXT_RUN";
	
	public TreasureManager()
	{
		nextDate();
	}
	
	private void updateTreasure()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM castle"))
		{
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int treasure = rs.getInt("dynamicTreasury");
					int castleId = rs.getInt("id");
					if (treasure > 0)
					{
						Castle castle = CastleManager.getInstance().getCastleById(castleId);
						castle.addToTreasuryNoTax(treasure);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE castle SET dynamicTreasury = ?"))
		{
			ps.setLong(1, 0);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		
		nextDate();
	}
	
	private void nextDate()
	{
		final Calendar calendar = Calendar.getInstance();
		final long nextRun = GlobalVariablesManager.getInstance().getLong(TREASURE_MANAGER_NEXT_RUN_VAR, 0);
		final boolean lastRun = nextRun > System.currentTimeMillis();
		final int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		if (!lastRun)
		{
			int days = Calendar.MONDAY - weekday;
			if (days <= 0)
			{
				days += 7;
			}
			calendar.add(Calendar.DAY_OF_YEAR, days);
			GlobalVariablesManager.getInstance().set(TREASURE_MANAGER_NEXT_RUN_VAR, calendar.getTimeInMillis());
			ThreadPool.schedule(this::updateTreasure, 15000);
			// LOGGER.info("Last TreasureManager run was " + new Date(nextRun));
		}
		else
		{
			long next = GlobalVariablesManager.getInstance().getLong(TREASURE_MANAGER_NEXT_RUN_VAR);
			ThreadPool.schedule(this::updateTreasure, next - System.currentTimeMillis());
			// LOGGER.info("Next TreasureManager run " + new Date(next));
		}
	}
	
	public static TreasureManager getInstance()
	{
		return TreasureManager.SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TreasureManager INSTANCE = new TreasureManager();
	}
}
