package io.github.legentpc.neu21plus.skyblock;

public class SkyBlockTime {

    private static final long SKYBLOCK_EPOCH = 1560275700000L;

    private static final long SKYBLOCK_YEAR_MS = 446400000L;

    private static final int MONTHS_PER_YEAR = 12;

    private static final int DAYS_PER_MONTH = 31;

    private static final int HOURS_PER_DAY = 24;

    private static final int MINUTES_PER_HOUR = 60;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public SkyBlockTime(long realTimeMillis) {
        long elapsed = realTimeMillis - SKYBLOCK_EPOCH;
        if (elapsed < 0) {
            elapsed = 0;
        }

        long totalMinutes = elapsed / (1000 * 50);

        this.year = (int) (totalMinutes / (MINUTES_PER_HOUR * HOURS_PER_DAY * DAYS_PER_MONTH * MONTHS_PER_YEAR)) + 1;
        long remaining = totalMinutes % (MINUTES_PER_HOUR * HOURS_PER_DAY * DAYS_PER_MONTH * MONTHS_PER_YEAR);

        this.month = (int) (remaining / (MINUTES_PER_HOUR * HOURS_PER_DAY * DAYS_PER_MONTH)) + 1;
        remaining = remaining % (MINUTES_PER_HOUR * HOURS_PER_DAY * DAYS_PER_MONTH);

        this.day = (int) (remaining / (MINUTES_PER_HOUR * HOURS_PER_DAY)) + 1;
        remaining = remaining % (MINUTES_PER_HOUR * HOURS_PER_DAY);

        this.hour = (int) (remaining / MINUTES_PER_HOUR);
        this.minute = (int) (remaining % MINUTES_PER_HOUR);
    }

    public static SkyBlockTime now() {
        return new SkyBlockTime(System.currentTimeMillis());
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getMonthName() {
        String[] names = {
                "Early Spring", "Spring", "Late Spring",
                "Early Summer", "Summer", "Late Summer",
                "Early Autumn", "Autumn", "Late Autumn",
                "Early Winter", "Winter", "Late Winter"
        };
        int index = Math.max(0, Math.min(month - 1, names.length - 1));
        return names[index];
    }

    public String getSeasonName() {
        String[] seasons = {"Spring", "Spring", "Spring", "Summer", "Summer", "Summer",
                "Autumn", "Autumn", "Autumn", "Winter", "Winter", "Winter"};
        int index = Math.max(0, Math.min(month - 1, seasons.length - 1));
        return seasons[index];
    }

    @Override
    public String toString() {
        return String.format("Year %d, %s %d, %02d:%02d", year, getMonthName(), day, hour, minute);
    }
}
