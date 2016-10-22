package com.hy.financial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author hyang
 *         Created on: Aug 22, 2011
 */
public class client {
    private static String url = "http://ichart.finance.yahoo.com/table.csv?&g=d&ignore=.csv";
    private static SortedSet<Quote> quoteSet = new TreeSet<Quote>();
    private static String filename = "symbol_pool";
    private static String portfolio_file = "portfolio";
    private static String bt_pool = "bt_pool";
    // 64 days are roughly 3 months.
    private static int DAY_COUNT = 64;

    private static Set<String> portfolio = new HashSet<String>();

    public static void main(String[] args) {
        Calendar now = Calendar.getInstance();
        // now.add(Calendar.DAY_OF_MONTH, -1);
        url = url + "&e=" + now.get(Calendar.DAY_OF_MONTH);
        url = url + "&d=" + now.get(Calendar.MONTH);
        url = url + "&f=" + now.get(Calendar.YEAR);
        now.add(Calendar.MONTH, -4);
        url = url + "&b=" + now.get(Calendar.DAY_OF_MONTH);
        url = url + "&a=" + now.get(Calendar.MONTH);
        url = url + "&c=" + now.get(Calendar.YEAR);
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
            if (portfolio.contains(q.getSymbol()))
                System.out.print("==> ");
            System.out.println(q + " " + ++count);
        }
    }

    private static void readOne(String symbol, boolean isBT) {
        BufferedReader rd = null;
        BufferedWriter writer = null;
        String fileName = "data/" + symbol + ".txt";
        try {
            FileReader fr = new FileReader(fileName);
            rd = new BufferedReader(fr);
        } catch (FileNotFoundException e) {
            System.out.println("Reading " + symbol + " from the internet...");
        }
        if (rd == null) {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url + "&s=" + symbol);
            try {
                FileWriter outFile = new FileWriter(fileName);
                writer = new BufferedWriter(outFile);
                HttpResponse response = client.execute(get);
                rd = new BufferedReader(new InputStreamReader(
                        response.getEntity().getContent()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            boolean result = readContent(symbol, rd, writer, isBT);

            rd.close();
            if (writer != null) {
                writer.close();
            }

            if (!result) {
                // bad file - delete it.
                File file = new File(fileName);
                file.delete();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*
     * @return true for good, false for bad
     */
    private static boolean readContent(String symbol, BufferedReader rd, BufferedWriter writer, boolean isBT) {
        String line = "";
        int count = 0;
        float todayPrice = 1;
        float earlyPrice = 1;
        float weekAgoPrice = 1;
        String today = null;
        String day65 = null;
        try {
            while ((line = rd.readLine()) != null) {
                if (writer != null) {
                    writer.write(line);
                    writer.write("\n");
                }
                if (count == 1) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    for (int i = 0; i < 7; i++) {
                        String s = st.nextToken();
                        if (i == 0)
                            today = s;
                        if (i == 6)
                            todayPrice = Float.parseFloat(s);
                    }
                }
                if (count == 6) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    for (int i = 0; i < 7; i++) {
                        String s = st.nextToken();
                        if (i == 6)
                            weekAgoPrice = Float.parseFloat(s);
                    }
                }
                if (count == DAY_COUNT) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    for (int i = 0; i < 7; i++) {
                        String s = st.nextToken();
                        if (i == 0)
                            day65 = s;
                        if (i == 6)
                            earlyPrice = Float.parseFloat(s);
                    }
                }
                count++;
            }
            Quote q = new Quote(symbol, today, todayPrice, day65, earlyPrice, (todayPrice / earlyPrice * 100 - 100), isBT,
                    (todayPrice / weekAgoPrice * 100 - 100));
            quoteSet.add(q);
            System.out.println(q);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchElementException e) {
            System.out.println("read bad content: " + line);
            return false;
        }
    }
}
