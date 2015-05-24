package com.grishberg.goodtube.data.containers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by G on 18.05.15.
 */
public class VideoContainer
{
	private String 	id;
	private Date 	publishedAt;
	private String	title;
	private String 	thumbnailUrl;
	private String	description;
	private String	duration;
	private long	viewCount;
	private long	likeCount;
	private	long	dislikeCount;

	public long getLikeCount()
	{
		return likeCount;
	}

	public void setLikeCount(long likeCount)
	{
		this.likeCount = likeCount;
	}

	public long getDislikeCount()
	{
		return dislikeCount;
	}

	public void setDislikeCount(long dislikeCount)
	{
		this.dislikeCount = dislikeCount;
	}

	public String getId()
	{
		return id;
	}

	public Date getPublishedAt()
	{
		return publishedAt;
	}

	public String getTitle()
	{
		return title;
	}

	public String getThumbnailUrl()
	{
		return thumbnailUrl;
	}

	public String getDescription()
	{
		return description;
	}

	public String getPublishedAtString()
	{
		DateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		return simpleDateFormat.format(publishedAt);
	};
	// ----- SETTERS ----------

	public void setId(String id)
	{
		this.id = id;
	}

	public void setPublishedAt(Date publishedAt)
	{
		this.publishedAt = publishedAt;
	}

	public void setPublishedAt(long publishedAtLong)
	{
		this.publishedAt = new Date( publishedAtLong);
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setThumbnailUrl(String thumbnailUrl)
	{
		this.thumbnailUrl = thumbnailUrl;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getViewCountStr()
	{
		String prefix = "";

		switch ( (int)(viewCount % 10) )
		{
			case 0:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 11:
			case 12:
			case 13:
			case 14:
				prefix	= "просмотров";
				break;

			case 1:
				prefix	= "просмотр";
				break;

			case 2:
			case 3:
			case 4:
				prefix	= "просмотра";
				break;
		}
		return String.format("%d %s",viewCount, prefix);
	}

	public long getViewCount()
	{
		return viewCount;
	}

	public void setViewCount(long viewCount)
	{

		this.viewCount = viewCount;
	}

	public String getDuration()
	{
		return duration;
	}

	public void setDuration(String youtubeDuration)
	{
		try
		{
			this.duration = timeHumanReadable(youtubeDuration);
		}
		catch (Exception e)
		{

		}
	}


	private String timeHumanReadable (String youtubeTimeFormat)
	{
// Gets a PThhHmmMssS time and returns a hh:mm:ss time

		String
				temp = "",
				hour = "",
				minute = "",
				second = "",
				returnString;

		// Starts in position 2 to ignore P and T characters
		for (int i = 2; i < youtubeTimeFormat.length(); ++ i)
		{
			// Put current char in c
			char c = youtubeTimeFormat.charAt(i);

			// Put number in temp
			if (c >= '0' && c <= '9')
				temp = temp + c;
			else
			{
				// Test char after number
				switch (c)
				{
					case 'H' : // Deal with hours
						// Puts a zero in the left if only one digit is found
						if (temp.length() == 1) temp = "0" + temp;

						// This is hours
						hour = temp;

						break;

					case 'M' : // Deal with minutes
						// Puts a zero in the left if only one digit is found
						if (temp.length() == 1) temp = "0" + temp;

						// This is minutes
						minute = temp;

						break;

					case  'S': // Deal with seconds
						// Puts a zero in the left if only one digit is found
						if (temp.length() == 1) temp = "0" + temp;

						// This is seconds
						second = temp;

						break;

				} // switch (c)

				// Restarts temp for the eventual next number
				temp = "";

			} // else

		} // for

		if (hour == "" && minute == "") // Only seconds
			returnString = second + " c.";
		else {
			if (hour == "") // Minutes and seconds
				returnString = minute + ":" + second;
			else // Hours, minutes and seconds
				returnString = hour + ":" + minute + ":" + second;
		}

		// Returns a string in hh:mm:ss format
		return returnString;
	}
}
