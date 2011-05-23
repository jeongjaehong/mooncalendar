package org.nilriri.LunaCalendar.tools;

import java.util.Calendar;

public class Lunar2Solar {

    private static final int[][] kk = new int[][] {
    /*1881*/{ 1, 2, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2, 1 }, { 1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 0 }, { 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2, 0 }, { 2, 1, 1, 2, 1, 3, 2, 1, 2, 2, 1, 2, 2 }, { 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 0 }, { 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 0 }, { 2, 2, 1, 2, 3, 2, 1, 1, 2, 1, 2, 1, 2 }, { 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 0 }, { 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 3, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2 },
    /*1891*/{ 1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 0 }, { 1, 1, 2, 1, 1, 2, 3, 2, 2, 1, 2, 2, 2 }, { 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 0 }, { 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 0 }, { 2, 1, 2, 1, 2, 3, 1, 2, 1, 2, 1, 2, 1 }, { 2, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 0 }, { 2, 1, 2, 3, 2, 2, 1, 2, 1, 2, 1, 2, 1 }, { 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 0 }, { 1, 2, 1, 1, 2, 1, 2, 2, 3, 2, 2, 1, 2 },
    /*1901*/{ 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 0 }, { 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 0 }, { 1, 2, 1, 2, 1, 3, 2, 1, 1, 2, 2, 1, 2 }, { 2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 0 }, { 2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 2, 1, 4, 1, 2, 1, 2, 1, 2, 1, 2 }, { 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 0 }, { 2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2, 0 }, { 1, 2, 3, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2 }, { 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 0 },
    /*1911*/{ 2, 1, 2, 1, 1, 2, 3, 1, 2, 2, 1, 2, 2 }, { 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 0 }, { 2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 0 }, { 2, 2, 1, 2, 2, 3, 1, 2, 1, 2, 1, 1, 2 }, { 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 0 }, { 2, 1, 3, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1 }, { 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2, 0 }, { 1, 2, 1, 1, 2, 1, 2, 3, 2, 2, 1, 2, 2 }, { 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2, 0 },
    /*1921*/{ 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 0 }, { 2, 1, 2, 2, 1, 3, 2, 1, 1, 2, 1, 2, 2 }, { 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 0 }, { 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1, 0 }, { 2, 1, 2, 2, 3, 2, 1, 2, 2, 1, 2, 1, 2 }, { 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 0 }, { 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 0 }, { 1, 2, 3, 1, 2, 1, 1, 2, 2, 1, 2, 2, 2 }, { 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 0 }, { 1, 2, 2, 1, 1, 2, 3, 1, 2, 1, 2, 2, 1 },
    /*1931*/{ 2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 0 }, { 2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 0 }, { 1, 2, 2, 1, 2, 4, 1, 2, 1, 2, 1, 1, 2 }, { 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 0 }, { 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 0 }, { 2, 1, 1, 4, 1, 2, 1, 2, 1, 2, 2, 2, 1 }, { 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 0 }, { 2, 2, 1, 1, 2, 1, 1, 4, 1, 2, 2, 1, 2 }, { 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 0 }, { 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 0 },
    /*1941*/{ 2, 2, 1, 2, 2, 1, 4, 1, 1, 2, 1, 2, 1 }, { 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 1, 2, 0 }, { 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 0 }, { 1, 1, 2, 1, 4, 1, 2, 1, 2, 2, 1, 2, 2 }, { 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2, 0 }, { 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 0 }, { 2, 2, 3, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2 }, { 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 0 }, { 2, 2, 1, 2, 1, 2, 1, 3, 2, 1, 2, 1, 2 }, { 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 0 },
    /*1951*/{ 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 1, 2, 1, 4, 2, 1, 2, 1, 2, 1, 2 }, { 1, 2, 1, 1, 2, 2, 1, 2, 2, 1, 2, 2, 0 }, { 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 0 }, { 2, 1, 1, 4, 1, 1, 2, 1, 2, 1, 2, 2, 2 }, { 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 0 }, { 2, 1, 2, 1, 2, 1, 1, 2, 3, 2, 1, 2, 2 }, { 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 0 }, { 2, 1, 2, 1, 2, 2, 3, 2, 1, 2, 1, 2, 1 },
    /*1961*/{ 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 0 }, { 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 0 }, { 2, 1, 2, 1, 3, 2, 1, 2, 1, 2, 2, 2, 1 }, { 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 0 }, { 1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 0 }, { 2, 2, 2, 3, 2, 1, 1, 2, 1, 1, 2, 2, 1 }, { 2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 2, 1, 2, 1, 2, 3, 2, 1, 2, 1, 2 }, { 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 0 }, { 2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2, 0 },
    /*1971*/{ 1, 2, 1, 1, 2, 3, 2, 1, 2, 2, 2, 1, 2 }, { 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 0 }, { 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 1, 0 }, { 2, 2, 1, 2, 3, 1, 2, 1, 1, 2, 2, 1, 2 }, { 2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 0 }, { 2, 2, 1, 2, 1, 2, 1, 2, 3, 2, 1, 1, 2 }, { 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 0 }, { 2, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 0 }, { 2, 1, 1, 2, 1, 2, 4, 1, 2, 2, 1, 2, 1 }, { 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 0 },
    /*1981*/{ 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2, 0 }, { 2, 1, 2, 1, 3, 2, 1, 1, 2, 2, 1, 2, 2 }, { 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 0 }, { 2, 1, 2, 2, 1, 1, 2, 1, 1, 2, 3, 2, 2 }, { 1, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 0 }, { 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1, 0 }, { 2, 1, 2, 2, 1, 2, 3, 2, 2, 1, 2, 1, 2 }, { 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 0 }, { 2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 0 }, { 1, 2, 1, 1, 2, 3, 1, 2, 1, 2, 2, 2, 2 },
    /*1991*/{ 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 0 }, { 1, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 0 }, { 1, 2, 2, 3, 2, 1, 2, 1, 1, 2, 1, 2, 1 }, { 2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 0 }, { 1, 2, 2, 1, 2, 2, 1, 2, 3, 2, 1, 1, 2 }, { 1, 2, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2, 0 }, { 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 0 }, { 2, 1, 1, 2, 1, 3, 2, 2, 1, 2, 2, 2, 1 }, { 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 0 }, { 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 0 },
    /*2001*/{ 2, 2, 2, 1, 3, 2, 1, 1, 2, 1, 2, 1, 2 }, { 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 0 }, { 2, 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 0 }, { 1, 2, 3, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2 }, { 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 0 }, { 1, 1, 2, 1, 2, 1, 2, 3, 2, 2, 1, 2, 2 }, { 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2, 0 }, { 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 0 }, { 2, 2, 1, 1, 2, 3, 1, 2, 1, 2, 1, 2, 2 }, { 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 0 },
    /*2011*/{ 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 0 }, { 2, 1, 2, 4, 2, 1, 2, 1, 1, 2, 1, 2, 1 }, { 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 1, 2, 1, 2, 1, 2, 2, 3, 2, 1, 2 }, { 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2, 2, 0 }, { 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 0 }, { 2, 1, 1, 2, 1, 3, 2, 1, 2, 1, 2, 2, 2 }, { 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 0 }, { 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 0 }, { 2, 1, 2, 2, 3, 2, 1, 1, 2, 1, 2, 1, 2 },
    /*2021*/{ 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 0 }, { 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 0 }, { 1, 2, 3, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2 }, { 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 0 }, { 2, 1, 2, 1, 1, 2, 3, 2, 1, 2, 2, 2, 1 }, { 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 0 }, { 1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 0 }, { 1, 2, 2, 1, 2, 3, 1, 2, 1, 1, 2, 2, 1 }, { 2, 2, 1, 2, 2, 1, 1, 2, 1, 1, 2, 2, 0 }, { 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 0 },
    /*2031*/{ 2, 1, 2, 3, 2, 1, 2, 2, 1, 2, 1, 2, 1 }, { 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 0 }, { 1, 2, 1, 1, 2, 1, 2, 3, 2, 2, 2, 1, 2 }, { 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 0 }, { 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 0 }, { 2, 2, 1, 2, 1, 1, 4, 1, 1, 2, 1, 2, 2 }, { 2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 0 }, { 2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 1, 0 }, { 2, 2, 1, 2, 2, 3, 2, 1, 2, 1, 2, 1, 1 }, { 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1, 0 },
    /*2041*/{ 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 0 }, { 1, 2, 3, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2 }, { 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2, 0 } };

    public static String s2l(Calendar c) {
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        return s2l(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    public static String s2l(int Year, int Month, int Day) {
        int m[] = new int[] { 31, 00, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        int dt[] = new int[163];
        int m1;
        int m2;
        int jcount;

        int td;
        int td0;
        int td1;
        int td2;
        int k11;

        boolean leap;

        int LYear = 0;
        int LMonth = 0;
        int LDay = 0;

        int idx = 0;
        for (int i = 0; i <= 162; i++) {
            dt[i] = 0;
            for (int j = 0; j <= 11; j++) {
                switch (kk[i][j]) {
                    case 1:
                    case 3:
                        dt[i] = dt[i] + 29;
                        break;
                    case 2:
                    case 4:
                        dt[i] = dt[i] + 30;
                        break;
                }
            }
            switch (kk[i][12]) {
                case 1:
                case 3:
                    dt[i] = dt[i] + 29;
                    break;
                case 2:
                case 4:
                    dt[i] = dt[i] + 30;
                    break;
            }

        }
        td1 = 1880 * 365 + Math.abs(1880 / 4) - Math.abs(1880 / 100) + Math.abs(1880 / 400) + 30;

        k11 = (Year - 1);
        td2 = k11 * 365 + Math.abs(k11 / 4) - Math.abs(k11 / 100) + Math.abs(k11 / 400);
        leap = ((Year % 400) == 0) || ((Year % 100) != 0) && ((Year % 4) == 0);
        if (leap)
            m[1] = 29;
        else
            m[1] = 28;

        for (int i1 = 0; i1 <= Month - 2; i1++) {
            td2 = td2 + m[i1];
        }

        td2 = td2 + Day;

        td = td2 - td1 + 1;

        td0 = dt[0];

        for (idx = 0; idx <= 162; idx++) {
            if (td <= td0)
                break;
            td0 = td0 + dt[idx + 1];

        }

        LYear = idx + 1881;
        td0 = td0 - dt[idx];
        td = td - td0;
        if (kk[idx][12] != 0)
            jcount = 13;
        else
            jcount = 12;
        m2 = 0;

        for (int j = 0; j <= jcount - 1; j++) {
            if (kk[idx][j] <= 2)
                m2 = m2 + 1;
            if (kk[idx][j] <= 2)
                m1 = kk[idx][j] + 28;
            else
                m1 = kk[idx][j] + 26;
            if (td <= m1)
                break;
            td = td - m1;
        }
        LMonth = m2;
        LDay = td;

        return String.format("%04d%02d%02d", LYear, LMonth, LDay);

    }

    public static String l2s(String LunarYear, String LunarMonth, String LunarDay) {
        return l2s(Integer.parseInt(LunarYear), Integer.parseInt(LunarMonth), Integer.parseInt(LunarDay));
    }

    public static String l2s(int LunarYear, int LunarMonth, int LunarDay) {

        int m[] = new int[] { 31, 00, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

        int m1 = 0;
        int m2 = 0;
        int n2 = 0;
        int i = 0;
        int j = 0;

        boolean leap = false;
        int DayTotal = 0;

        if (LunarYear != 1881) {
            m1 = LunarYear - 1882;
            for (i = 0; i <= m1; i++) {
                for (j = 0; j <= 12; j++)
                    DayTotal = DayTotal + kk[i][j];
                if (kk[i][12] == 0)
                    DayTotal = DayTotal + 336;
                else
                    DayTotal = DayTotal + 362;
            }
        }
        m1 = m1 + 1;
        n2 = LunarMonth - 1;
        m2 = 0;
        while (true) {

            if (kk[m1][m2] > 2) {
                DayTotal = DayTotal + 26 + kk[m1][m2];
                n2 = n2 + 1;
            } else if (m2 == n2) {
                break;
            } else {
                DayTotal = DayTotal + 28 + kk[m1][m2];
            }

            m2 = m2 + 1;
        }

        DayTotal = DayTotal + LunarDay + 29;

        m1 = 1880;
        while (true) {
            m1 = m1 + 1;
            leap = ((m1 % 400) == 0) || ((m1 % 100) != 0) && ((m1 % 4) == 0);
            if (leap)
                m2 = 366;
            else
                m2 = 365;
            if (DayTotal <= m2)
                break;
            DayTotal = DayTotal - m2;
        }

        int SolarYear = m1;

        m[1] = m2 - 337;

        m1 = 0;
        while (true) {
            if (DayTotal <= m[m1])
                break;
            DayTotal = DayTotal - m[m1];
            m1 = m1 + 1;
        }
        int SolarMonth = m1 + 1;
        int SolarDay = DayTotal;

        return String.format("%04d%02d%02d", SolarYear, SolarMonth, SolarDay);
    }

    public static int getLunarID(String lday) {

        int y, m, d;

        y = Integer.parseInt(lday.substring(0, 4));
        m = Integer.parseInt(lday.substring(4, 6));
        d = Integer.parseInt(lday.substring(6, 8));
        float moon = CalculateMoonPhase(y, m, d);

        return Math.round(moon);
    }

    public static float CalculateMoonPhase(int y, int m, int d) {
        float delta = 0, moon = 0;
        int ry;

        if (y < 1900 || y > 2099)
            return -1;

        switch (y / 100) {
            case 19:
                delta = -4;
                break;
            case 20:
                delta = -8.3f;
                break;
            default:
                break;
        }

        ry = y % 100;
        ry %= 19;
        if (ry > 9)
            ry -= 19;
        ry *= 11;
        ry %= 30;
        if (m < 3)
            m += 2;

        moon = (30 + ry + m + d + delta) % 30f;
        return moon;
    }

}
