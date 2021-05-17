package com.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class DateTimeUtil {
	public static String FORMAT_DATE = "yyyy-MM-dd";
	public static String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";
	public static String FORMAT_HOUR = "yyyy-MM-dd HH:mm";
	public static String FORMAT_TIME_STR = "yyyyMMdd_HHmmss";
	public static String FORMAT_INT_DATE = "yyyyMMdd";
	public static String FORMAT_DATE_YEAR_MONTH = "yyyyMM";

	public final static int ONE_DAY = 24 * 60 * 60 * 1000;
	public final static int TWO_DAY = 2 * 24 * 60 * 60 * 1000;
	public final static int ONE_HOUR = 60 * 60 * 1000;
	public final static int TEN_MINUTES = 10 * 60 * 1000;

	/**
	 * all DateFormat of date and time
	 * 
	 * @param date
	 * @return
	 */
	public static String formatTime(final long timeInMillis) {
		return formatDateTime(timeInMillis, FORMAT_TIME);
	}

	public static String formatHour(final long timeInMillis) {
		return formatDateTime(timeInMillis, FORMAT_HOUR);
	}

	public static String formatDateTime(final long timeInMillis, String format) {
		SimpleDateFormat fm = new SimpleDateFormat(format);
		return fm.format(new Date(timeInMillis));
	}

	public static String formatDate(long timeInMillis) {
		return formatDateTime(timeInMillis, FORMAT_DATE);
	}

	public static String formatYearMonthDate(long timeInMillis) {
		return formatDateTime(timeInMillis, FORMAT_DATE_YEAR_MONTH);
	}

	public static boolean isInClock24() {
		return isInDestClock24(System.currentTimeMillis());
	}

	public static boolean isInDestClock24(long destDate) {
		return System.currentTimeMillis() < DateTimeUtil
				.getClock24Time(destDate);
	}

	public static long getClock0Time(long destTime) {
		return getTimeInMillis(destTime, 0, 0, 1);
	}
	
	public static long getClock24Time(long destTime) {
		return getTimeInMillis(destTime, 23, 59, 59);
	}

	public static long getDateTime(long destTime) {
		String formatDate = formatDate(destTime);
		return parseDate(formatDate);
	}
	
	public static long getDateTime2(long destTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(destTime);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}

	public static long getDateNextDay(long destTime) {
		return getClock24Time(destTime) + 1000;
	}

	public static boolean isSameDate(long time1, long time2) {
		Date date1 = new Date(time1);
		Date date2 = new Date(time2);
		return date1.getDate() == date2.getDate()
				&& date1.getMonth() == date2.getMonth()
				&& date1.getYear() == date2.getYear();
	}

	public static long getTimeInMillis(long srcTime) {
		return getTimeInMillis(srcTime, 0, 0, 0);
	}

	public static long getTimeInMillis(long srcTime, int hour, int minute,
			int second) {
		try {
			Date date = new Date(srcTime);
			date.setHours(hour);
			date.setMinutes(minute);
			date.setSeconds(second);
			return date.getTime();
		} catch (Exception e) {
			return 0;
		}
	}

	public static boolean isToday(long destTime) {
		return isToday(new Date(destTime));
	}
	
	public static boolean isTomorrow(long destTime){
		return isTomorrowDay(new Date(destTime));
	}

	public static boolean isToday(Date destTime) {
		int year = destTime.getYear();
		int month = destTime.getMonth();
		int day = destTime.getDate();
		Date now = new Date();
		if (year == now.getYear() && month == now.getMonth()
				&& day == now.getDate()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isYesterday(Date destTime) {
		int year = destTime.getYear();
		int month = destTime.getMonth();
		int day = destTime.getDate();
		Date now = new Date(Calendar.getInstance().getTimeInMillis() - ONE_DAY);
		if (year == now.getYear() && month == now.getMonth()
				&& day == now.getDate()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isTomorrowDay(Date destTime) {
		int year = destTime.getYear();
		int month = destTime.getMonth();
		int day = destTime.getDate();
		Date now = new Date(Calendar.getInstance().getTimeInMillis() + ONE_DAY);
		if (year == now.getYear() && month == now.getMonth()
				&& day == now.getDate()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isWeeksShouShow(Date destTime) {

		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if ((dayOfWeek >= 4 && dayOfWeek <= 7) || dayOfWeek == 1) {
			calendar.set(date.getYear() + 1900, date.getMonth(),
					date.getDate() - 2, 23, 59, 59);
			Long end = calendar.getTimeInMillis();
			int lose = 0;
			if (dayOfWeek == 4) {
				lose = 2;
			} else if (dayOfWeek == 5) {
				lose = 3;
			} else if (dayOfWeek == 6) {
				lose = 4;
			} else if (dayOfWeek == 7) {
				lose = 5;
			} else {
				lose = 6;
			}
			int year = date.getYear() + 1900;
			int month = date.getMonth();
			int day = date.getDate() - lose;
			if (day <= 0) {
				month -= 1;
				if (month <= -1) {
					year -= 1;
					month = 11;
				}
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month);
				int maxDate = cal.getActualMaximum(Calendar.DATE);
				day += maxDate;
			}
			calendar.set(year, month, day, 0, 0, 0);
			Long begin = calendar.getTimeInMillis();
			long tasktime = destTime.getTime();
			if (tasktime >= begin && tasktime <= end) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get Years(周岁)
	 * 
	 * @param birthTimeInMillis
	 * @return
	 */
	public static int getAge(long birthTimeInMillis) {
		return caculateYearsOfAge(birthTimeInMillis, System.currentTimeMillis());
	}
	
	public static int getIntervalDays(long previousMillis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int nowYear = cal.get(Calendar.YEAR);
		int nowDay = cal.get(Calendar.DAY_OF_YEAR);
		// previous time
		cal.setTimeInMillis(previousMillis);
		int prevYear = cal.get(Calendar.YEAR);
		int prevDay = cal.get(Calendar.DAY_OF_YEAR);
		if (nowYear > prevYear) {
			nowDay = (nowYear - prevYear) * 365;
		}
		int diffDay = nowDay - prevDay;
		return diffDay;
	}

	public static int caculatePeriodDays(long previousMillis) {
		Date date = new Date(previousMillis);
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		long deffTime = System.currentTimeMillis() - date.getTime();
		return (int) Math.ceil(deffTime / ONE_DAY);
	}

	public static int caculateYearsOfAge(long prevMillis, long nextMillis) {
		Date pDate = new Date(prevMillis);
		int pYear = pDate.getYear();
		int pMonth = pDate.getMonth();
		int pDateDay = pDate.getDate();
		Date nDate = new Date(nextMillis);
		int nYear = nDate.getYear();
		int nMonth = nDate.getMonth();
		int nDateDay = nDate.getDate();
		int diffYears = Math.abs(nYear - pYear);
		if (diffYears > 0 && nMonth < pMonth
				|| (nMonth == pMonth && nDateDay < pDateDay)) {
			diffYears--;
			Log.i("hulk", "birthday: " + pDate.toLocaleString());
		}
		return diffYears;
	}

	public static int caculateDiffDays(long previousMillis, long nextMillis) {
		long diff = Math.abs(nextMillis - previousMillis);
		int diffDays = (int) Math.floor(diff / ONE_DAY);
		return diffDays;
	}

	public static int getAge(String dayTime) {
		return getAge(parseDate(dayTime));
	}

	public static Date getDate(long timeInMillis, String format) {
		SimpleDateFormat fm = new SimpleDateFormat(format);
		try {
			String formatDate = formatDateTime(timeInMillis, format);
			return fm.parse(formatDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static long getDateTime(String formatDate) {
		return parseDate(formatDate);
	}
	
	public static long getDateTime(long timeInMillis, String format) {
		return getDate(timeInMillis, format).getTime();
	}

	public static long parseDate(String formatDate) {
		return parseTimeBase(formatDate, FORMAT_DATE);
	}

	/**
	 * 
	 * @param formatDate yyyy-MM-dd 
	 * @return  yyyyMMdd
	 */
	public static int parseAsIntDate(String formatDate) {
		String intDate = formatDate.replace("-", "");
		return Integer.valueOf(intDate);
	}

	/**
	 * yyyyMMdd
	 * @param formatDate String: yyyyMMdd
	 * @return long time stamp 1360909989800
	 */
	public static long parseIntDate(String formatIntDate) {
		return  parseTimeBase(formatIntDate, FORMAT_INT_DATE);
	}

	/**
	 * yyyyMMdd
	 * @param formatDate int: yyyyMMdd
	 * @return long time stamp 1360909989800
	 */
	public static long parseIntDate(int formatIntDate) {
		return  parseTimeBase(formatIntDate + "", FORMAT_INT_DATE);
	}
	
	/**
	 * format as yyyyMMdd
	 * @param timeInMillis
	 * @return  yyyyMMdd
	 */
	public static String formatAsIntDate(long timeInMillis) {
		return formatDateTime(timeInMillis, FORMAT_INT_DATE);
	}
	
	/**
	 * format as yyyy-MM-dd
	 * @param intDate  yyyyMMdd
	 * @return yyyy-MM-dd
	 */
	public static String formatDate(int intDate) {
		long time = parseIntDate(intDate);
		return formatDateTime(time, FORMAT_DATE);
	}

	/**
	 * "yyyyMMdd"
	 * @param timeInMillis
	 * @return  int yyyyMMdd 
	 */
	public static int toIntDate(long timeInMillis) {
		return Integer.valueOf(formatDateTime(timeInMillis, FORMAT_INT_DATE));
	}

	/**
	 * 
	 * @param formatDate yyyy-MM-dd
	 * @return  yyyyMMdd
	 */
	public static int toAsIntDate(String formatDate) {
		return parseAsIntDate(formatDate);
	}
	
	public static int toAsIntDate(long timeInMillis) {
		return toIntDate(timeInMillis);
	}
	
	public static long toDateTimeInMillis(int formatIntDate) {
		return parseIntDate(String.valueOf(formatIntDate));
	}

	/**
	 * 
	 * @param string formatIntDate  yyyyMMdd 
	 * @return
	 */
	public static int toIntDate(String formatIntDate) {
		return Integer.valueOf(formatIntDate);
	}
	
	public static String toLocaleString(long milliseconds) {
		return new Date(milliseconds).toLocaleString();
	}

	/**
	 * 
	 * @param dateText   : "20140405" or "2014-04-05"
	 * @return  date timestamp
	 */
	public static long parseGenericDate(String formatDate) {
		long dateTimeStamp = 0;
		if(!formatDate.contains("-") && formatDate.length() == 8) {
			dateTimeStamp = DateTimeUtil.parseIntDate(formatDate);
		} else if(formatDate.contains("-") && formatDate.length() == 10){
			dateTimeStamp = DateTimeUtil.parseDate(formatDate);
		} else {
			throw new IllegalArgumentException("The formatDate length Is Invalid,must(8 or 10) !! ");
		}
		return dateTimeStamp;
	}

	/**
	 * 
	 * @param timeInMillis java timeInMillis orint date 20140405
	 * @param withConnector  with "-"
	 * @return   "20140405" or "2014-04-05"
	 */
	public static String formatGenericDate(long timeInMillis, boolean withConnector) {
		String formatDate = null;
		String timeStr = String.valueOf(timeInMillis);
		if(timeStr.length() == 13){
			if(withConnector) {
				formatDate = DateTimeUtil.formatDate(timeInMillis);
			} else {
				formatDate = DateTimeUtil.formatAsIntDate(timeInMillis);
			}
		} else if(timeStr.length() == 8) {
			formatDate = timeStr;
		} else {
		    formatDate = DateTimeUtil.formatDate(timeInMillis);
		}
		return formatDate;
	}

	public static long parseTime(String formatTime) {
		return parseTimeBase(formatTime, FORMAT_TIME);
	}
	
	public static long parseTimeHour(String formatTimeHour) {
		return parseTimeBase(formatTimeHour, FORMAT_HOUR);
	}

	public static long parseTimeToDate(String formatTime) {
		return parseTimeBase(formatTime, FORMAT_DATE);
	}

	public static long parseTimeBase(String formatTime, String format) {
		if (TextUtils.isEmpty(formatTime)) {
			return 0;
		}
		SimpleDateFormat fm = new SimpleDateFormat(format);
		try {
			Date date = fm.parse(formatTime);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean isNextDay(long lastTime) {
		return System.currentTimeMillis() > DateTimeUtil
				.getClock24Time(lastTime);
	}

	public static boolean isNextDay(long lastTime, int intervalDays) {
		return (System.currentTimeMillis() - intervalDays * ONE_DAY) > DateTimeUtil
				.getClock24Time(lastTime);
	}

	public static boolean isNextDayNow(long prevTime) {
		return isNextDayNow(System.currentTimeMillis(), prevTime);
	}

	public static boolean isNextDayNow(long currentTime, long previousTime) {
		if (currentTime > previousTime) {
			long diff = currentTime - previousTime;
			return diff > ONE_DAY;
		}
		return false;
	}

	public static boolean isNextHour(long prevTime) {
		return isNextHour(System.currentTimeMillis(), prevTime);
	}

	public static boolean isNextHour(long currentTime, long previousTime) {
		if (currentTime > previousTime) {
			long diff = currentTime - previousTime;
			return diff > ONE_HOUR;
		}
		return false;
	}

	public static boolean isNextTime(long lastTime, int interval) {
		long now = System.currentTimeMillis();
		if (now > lastTime) {
			long diff = now - lastTime;
			return diff > interval;
		}
		return false;
	}

	public static long getNextDayTime(long time) {
		return time + ONE_DAY;
	}

	public static long getPrevDayTime(long time) {
		return time - ONE_DAY;
	}

	/**
	 * change time period to "00:00:00"
	 */
	public static String formatTimePeriod(long time) {
		if (time < 0) {
			return "00:00:00";
		}
		String timeCount = "";
		long hourc = time / ONE_HOUR;
		String hour = String.valueOf(hourc);
		if (hour.length() < 2) {
			hour = "0" + hourc;
		}
		long minuec = (time % ONE_HOUR) / (60000);
		String minue = "0" + minuec;
		minue = minue.substring(minue.length() - 2, minue.length());
		long secc = (time % 60000) / 1000;
		String sec = "0" + secc;
		sec = sec.substring(sec.length() - 2, sec.length());
		timeCount = hour + " : " + minue + " : " + sec;
		return timeCount;
	}

	public static boolean isExpired(String date, String format) {
		return System.currentTimeMillis() > parseTimeBase(date, format);
	}

	public static boolean isExpired(long time) {
		return time < System.currentTimeMillis();
	}

	public static SimpleDateFormat getDateFormat(String format) {
		SimpleDateFormat fm = new SimpleDateFormat(format);
		return fm;
	}

	public static void pickDate(Context context, Calendar srCalendar, DatePickCallback cb) {
        pickDate(context, srCalendar, FORMAT_DATE, cb);
    }

	public static void pickDate(Context context, int theme, Calendar srCalendar, DatePickCallback cb) {
        pickDate(context, srCalendar, FORMAT_DATE, cb);
    }

	/**
	 * pick destination date and format date string according to source date.
	 * @param context
	 * @param scrCal
	 * @param format  eg: "yyyy-MM-dd" or eg: "yyyy-MM-dd HH:mm:ss"
	 * @param cb
	 */
	public static void pickDate(Context context, final Calendar scrCal,
	        final String format, final DatePickCallback cb) {
        OnDateSetListener dl = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker picker, int year, int monthOfYear, int dayOfMonth) {
                Calendar destCal = scrCal;
                destCal.set(Calendar.YEAR, year);
                destCal.set(Calendar.MONTH, monthOfYear);
                destCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if(cb != null) {
                    String formatDate = null;
                    if(format != null && !format.isEmpty()) {
                        formatDate = formatDateTime(destCal.getTime().getTime(), format);
                    }
                    cb.onDatePick(destCal , formatDate);
                }
            }
        };
        Calendar src = scrCal;
        if(src == null) {
            src = Calendar.getInstance();
        }
        int year = src.get(Calendar.YEAR);
        int month = src.get(Calendar.MONTH);
        int day = src.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(context, dl, year, month, day).show();
    }

	public static void pickTime(Context context, Calendar srCalendar, TimePickCallback callback) {
        pickTime(context, srCalendar, FORMAT_TIME, callback);
    }

	/**
	 * pick destination time and format time string according to source date.
	 * @param context
	 * @param scrCal
	 * @param format  eg: "yyyy-MM-dd HH:mm:ss"
	 * @param callback
	 */
	public static void pickTime(Context context, final Calendar scrCal, final String format, final TimePickCallback callback) {
        OnTimeSetListener tl = new OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar destCal = scrCal;
                destCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                destCal.set(Calendar.MINUTE, minute);
                String formatTime = null;
                if(callback != null) {
                    if(format != null && !format.isEmpty()) {
                        formatTime = formatDateTime(destCal.getTime().getTime(), format);
                    }
                    callback.onTimePick(destCal, hourOfDay, minute, formatTime);
                }
            }
        };
        Calendar src = scrCal;
        if(src == null) {
            src = Calendar.getInstance();
        }
        int hourOfDay = scrCal.get(Calendar.HOUR_OF_DAY);
        int minute = scrCal.get(Calendar.MINUTE);
        TimePickerDialog picker = new TimePickerDialog(context, tl, hourOfDay, minute, true);
        picker.show();
    }

	public interface DatePickCallback {
	    /**
	     * @param destCalendar
	     * @param formatDate  eg: "2015-09-10"
	     */
	    void onDatePick(Calendar destCalendar, String formatDate);
	}

	public interface TimePickCallback {
        void onTimePick(Calendar destCalendar, int hourOfDay, int minute, String formatTime);
    }
}
