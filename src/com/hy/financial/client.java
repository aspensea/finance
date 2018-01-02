package com.hy.financial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;

/**
 * @author hyang
 *         Created on: Aug 22, 2011
 */
public class client {
    // private static String url = "http://ichart.finance.yahoo.com/table.csv?&g=d&ignore=.csv";
    private static String url = "https://finance.yahoo.com/quote/";
    private static SortedSet<Quote> quoteSet = new TreeSet<Quote>();
    private static String filename = "symbol_pool";
    private static String portfolio_file = "portfolio";
    private static String bt_pool = "bt_pool";
    // 64 days are roughly 3 months.
    private static int DAY_COUNT = 63;

    private static Set<String> portfolio = new HashSet<String>();

    public static void main(String[] args) {
        Calendar now = Calendar.getInstance();
/*
        // now.add(Calendar.DAY_OF_MONTH, -1);
        url = url + "&e=" + now.get(Calendar.DAY_OF_MONTH);
        url = url + "&d=" + now.get(Calendar.MONTH);
        url = url + "&f=" + now.get(Calendar.YEAR);
        now.add(Calendar.MONTH, -4);
        url = url + "&b=" + now.get(Calendar.DAY_OF_MONTH);
        url = url + "&a=" + now.get(Calendar.MONTH);
        url = url + "&c=" + now.get(Calendar.YEAR);
*/
        System.out.println("URL: " + url);
        int count = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(portfolio_file));
            String s;
            while ((s = reader.readLine()) != null) {
                portfolio.add(s.trim());
            }
            reader.close();
            reader = new BufferedReader(new FileReader(filename));
            while ((s = reader.readLine()) != null) {
                System.out.print(++count + " ");
                readOne(s.trim(), false);
            }
            reader.close();
            reader = new BufferedReader(new FileReader(bt_pool));
            while ((s = reader.readLine()) != null) {
                System.out.print(++count + " ");
                readOne(s.trim(), true);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("=======================================");
        count = 0;
        for (Quote q : quoteSet) {
            System.out.println(String.valueOf(++count) + '\t' + q.toString() + (portfolio.contains(q.getSymbol())? "*P*" : ""));
        }
    }

    private static void readOne(String symbol, boolean isBT) throws IOException {
        BufferedReader rd = null;
        BufferedWriter writer = null;
        String fileName = "data/" + symbol + ".txt";
        StringBuilder sb = new StringBuilder();

        try {
            FileReader fr = new FileReader(fileName);
            rd = new BufferedReader(fr);
        } catch (FileNotFoundException e) {
            System.out.println("Reading " + symbol + " from the internet...");
            FileWriter outFile = new FileWriter(fileName);
            writer = new BufferedWriter(outFile);
        }
        if (rd == null) {
            HttpClient client = new DefaultHttpClient();
            // HttpGet get = new HttpGet(url + "&s=" + symbol);
            HttpGet get = new HttpGet(url+symbol+"/history");
            try {
                HttpResponse response = client.execute(get);
                rd = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String line;
        while ((line = rd.readLine())!= null) {
            sb.append(line+'\n');
            if (writer != null) {
                writer.write(line + '\n');
            }
        }

        try {
            boolean result = readContent(symbol, sb.toString(), isBT);
            if (!result) {
                System.out.println("Please investigate " + symbol);
            }
            rd.close();
            if (writer != null) {
                writer.close();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*
     * @return true for good, false for bad
     */
    private static boolean readContent(String symbol, final String content, boolean isBT) {

        String today = null;
        float todayPrice = 0;

        // For some reason Yahoo doesn't have the current day price in the range.
/*        if (symbol.length() == 5) {
            int todayIndex = -1; content.indexOf("D(ib)\" data-reactid=\"36\">");
            if (todayIndex == -1) {
                todayIndex = content.indexOf("D(ib)\" data-reactid=\"35\">");
                if (todayIndex == -1) {
                    todayIndex = content.indexOf("D(ib)\" data-reactid=\"15\">");
                    if (todayIndex == -1) {
                        System.err.println(symbol + " can't locate today's price");
                        return false;
                    }
                }
            }
            String tempStr = content.substring(todayIndex);

            tempStr = tempStr.substring(tempStr.indexOf(">")+1);
            if (tempStr.startsWith("<!")) {
                tempStr = tempStr.substring(tempStr.indexOf(">")+1);
            }

            try {
                todayPrice = Float.parseFloat(tempStr.substring(0, tempStr.indexOf("<")));
            } catch (NumberFormatException e) {
                System.out.println("Cannot parse to float: " + tempStr);
                return false;
            }
            today = "today";
        }
*/
        String parsed = Jsoup.parse(content).text();

        if (!parsed.contains(" Volume ")) {
            System.out.println("Unable to parse " + symbol + ": "  + parsed);
            return false;
        }
        parsed = parsed.substring(parsed.indexOf(" Volume ")+8, parsed.length());
        if (!parsed.contains(" *Close ")) {
            System.out.println("Unable to parse " + symbol + ": "  + parsed);
            return false;
        }
        parsed = parsed.substring(0, parsed.indexOf(" *Close "));
        String[] quoteData = parsed.split(" ");

        String line = "";
        int count = 0;
        float earlyPrice = 1;
        float weekAgoPrice = 1;
        String day65 = null;
        int quoteIndex = 0;
/*        if (symbol.length() == 5) {
            count++;
        }
*/
        while (quoteIndex + 9 <= quoteData.length) {
            if (quoteIndex + 5 <= quoteData.length && "Dividend".equals(quoteData[quoteIndex+4])) {
                quoteIndex = quoteIndex + 5;
            }
            if (quoteIndex + 5 <= quoteData.length && "Dividend".equals(quoteData[quoteIndex+4])) {
                quoteIndex = quoteIndex + 5;
            }
            int offset = 0;
            if (quoteIndex + 5 <= quoteData.length && "Split".equals(quoteData[quoteIndex+5])) {
                offset = 6;
            }
            try {
                if (count == 0) {
                    today = quoteData[quoteIndex] + " " + quoteData[quoteIndex + 1] + " " + quoteData[quoteIndex + 2];
                    todayPrice = Float.parseFloat(quoteData[quoteIndex + offset + 7]);
                }
                if (count == 5) {
                    weekAgoPrice = Float.parseFloat(quoteData[quoteIndex + offset + 7]);
                }
                if (count == DAY_COUNT) {
                    day65 = quoteData[quoteIndex] + " " + quoteData[quoteIndex + 1] + " " + quoteData[quoteIndex + 2];
                    earlyPrice = Float.parseFloat(quoteData[quoteIndex + offset + 7]);
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Cannot parse to float at " + quoteIndex + "/" + quoteData[quoteIndex+offset+7] + ": " + Arrays.toString(quoteData));
                return false;
            }
            count++;
            quoteIndex = quoteIndex + offset + 9;
        }
        Quote q = new Quote(symbol, today, todayPrice, day65, earlyPrice, weekAgoPrice,
                (todayPrice / earlyPrice * 100 - 100), isBT,
                (todayPrice / weekAgoPrice * 100 - 100));
        quoteSet.add(q);
        System.out.println(q);
        return true;
    }
}
