package core;

import org.junit.Test;

import javax.jws.soap.SOAPBinding;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LocalDateTest {

    @Test
    public void localDate() {

        LocalDateTime currentTime = LocalDateTime.now();

        System.out.println(currentTime);

        LocalDate localDate = currentTime.toLocalDate();

        System.out.println(localDate);
        LocalDate localDate1 = LocalDate.now();
        System.out.println(localDate.toString());

        Month month = currentTime.getMonth();
        System.out.println("month:" + month);

        int day = currentTime.getDayOfMonth();
        System.out.println("day:" + day);


        LocalDateTime date2 = currentTime.withDayOfMonth(10).withYear(2012);

        System.out.println(date2);


    }

    @Test
    public void oldFormat() {
        //format yyyy-MM-dd
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(now);
        System.out.println(String.format("date format : %s", date));
        //format HH:mm:ss
        SimpleDateFormat sdft = new SimpleDateFormat("HH:mm:ss");
        String time = sdft.format(now);
        System.out.println(String.format("time format : %s", time));

        //format yyyy-MM-dd HH:mm:ss
        SimpleDateFormat sdfdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = sdfdt.format(now);
        System.out.println(String.format("dateTime format : %s", datetime));

    }

    @Test
    public void newFormat() {
        //format yyyy-MM-dd
        LocalDate date = LocalDate.now();
        System.out.println(String.format("date format : %s", date));

        //format HH:mm:ss
        LocalTime time = LocalTime.now().withNano(0);
        System.out.println(String.format("time format : %s", time));

        //format yyyy-MM-dd HH:mm:ss
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTimeStr = dateTimeFormatter.format(dateTime);
        System.out.println(String.format("dateTime format : %s", dateTimeStr));

    }

    @Test
    public void oldTrans() throws ParseException {

        //已弃用
        Date date = new Date("2021-01-26");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = sdf.parse("2021-01-26");
    }

    @Test
    public void newTrans() throws ParseException {
        LocalDate date = LocalDate.of(2021, 1, 26);
        LocalDate.parse("2021-01-26");

        LocalDateTime dateTime = LocalDateTime.of(2021, 1, 26, 12, 12, 22);
        LocalDateTime.parse("2021-01-26 12:12:22");

        LocalTime time = LocalTime.of(12, 12, 22);
        LocalTime.parse("12:12:22");

        System.out.println(String.format("dateTime format : %s", dateTime));
    }

    @Test
    public void equal() throws ParseException {
        //方法1
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr1 = "2021-01-26 16:09:11";
        String timeStr2 = "2021-01-26 16:09:11";
        Date date1 = sdf.parse(timeStr1);
        Date date2 = sdf.parse(timeStr2);
        System.out.println(date1.compareTo(date2));
        System.out.println(date1.equals(date2));


        //方法2
        Calendar time1 = Calendar.getInstance();
        Calendar time2 = Calendar.getInstance();
        time1.setTime(date1);
        time2.setTime(date2);
        assert time1.equals(time2);

    }

    @Test
    public void newequal() throws ParseException {
        LocalDate localDate = LocalDate.now();
        LocalDate localDate2 = LocalDate.of(2021, 1, 26);
        assert localDate.equals(localDate2);
    }

    @Test
    public void afterDay() throws ParseException {

        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 7);
        Date d = ca.getTime();
        String after = formatDate.format(d);
        System.out.println("一周后日期：" + after);


        //算两个日期间隔多少天
        String dates1 = "2021-12-23";
        String dates2 = "2021-02-26";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = format.parse(dates1);
        Date date2 = format.parse(dates2);
        int day = (int) ((date1.getTime() - date2.getTime()) / (1000 * 3600 * 24));

        System.out.println(dates2 + "和" + dates2 + "相差" + day + "天");
    }


    @Test
    public void pushWeek() {
        LocalDate localDate = LocalDate.now();
        LocalDate after = localDate.plus(1, ChronoUnit.WEEKS);
        LocalDate after2 = localDate.plusWeeks(1);
        System.out.println("一周后日期：" + after);

        //算两个日期间隔多少天
        LocalDate date1 = LocalDate.parse("2021-02-26");
        LocalDate date2 = LocalDate.parse("2021-12-23");
        Period period = Period.between(date1, date2);
        System.out.println("date1 到 date2 相隔："
                + period.getYears() + "年"
                + period.getMonths() + "月"
                + period.getDays() + "天");

        long day = date2.toEpochDay() - date1.toEpochDay();
        System.out.println(date2 + "和" + date2 + "相差" + day + "天");


    }

    @Test
    public void getDay() {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前月第一天：
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        String first = format.format(c.getTime());
        System.out.println("first day:" + first);

        //获取当前月最后一天
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = format.format(ca.getTime());
        System.out.println("last day:" + last);

        //当年最后一天
        Calendar currCal = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, currCal.get(Calendar.YEAR));
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        Date time = calendar.getTime();
        System.out.println("last day:" + format.format(time));

    }

    @Test
    public void getDayNew() {

        LocalDate today = LocalDate.now();
        //获取当前月第一天：
        LocalDate firstDayOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        // 取本月最后一天
        LocalDate lastDayOfThisMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        //取下一天：
        LocalDate nextDay = lastDayOfThisMonth.plusDays(1);

        //当年最后一天
        LocalDate lastday = today.with(TemporalAdjusters.lastDayOfYear());


        LocalDate lastMondayOf2021 = LocalDate.parse("2021-12-31").with(TemporalAdjusters.lastInMonth(DayOfWeek.SUNDAY));

        System.out.println(lastMondayOf2021);


    }

    @Test
    public void convertToGmt() {

        //北京时间：Wed Jan 27 14:05:29 CST 2021
        Date date = new Date();

        SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //北京时区
        bjSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        System.out.println("毫秒数:" + date.getTime() + ", 北京时间:" + bjSdf.format(date));

        //东京时区
        SimpleDateFormat tokyoSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        tokyoSdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));  // 设置东京时区
        System.out.println("毫秒数:" + date.getTime() + ", 东京时间:" + tokyoSdf.format(date));

    }

    @Test
    public void testZonedDateTime(){

        //当前时区时间
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        System.out.println("当前时区: " + zonedDateTime);

        //东京时间
        ZoneId zoneId = ZoneId.of(ZoneId.SHORT_IDS.get("JST"));
        ZonedDateTime tokyoTime = zonedDateTime.withZoneSameInstant(zoneId);
        System.out.println("东京时间: " + tokyoTime);

        LocalDateTime localDateTime = tokyoTime.toLocalDateTime();
        System.out.println("东京时间转当地时间: " + localDateTime);


        ZonedDateTime localZoned = localDateTime.atZone(ZoneId.systemDefault());
        System.out.println("本地时区时间: " + localZoned);

    }

    @Test
    public void newDate(){
        System.out.println(new Date());
    }


}
