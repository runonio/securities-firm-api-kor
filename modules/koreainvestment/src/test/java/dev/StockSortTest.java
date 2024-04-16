package dev;

import io.runon.stock.trading.Stock;
import io.runon.stock.trading.StockCandles;
import io.runon.stock.trading.Stocks;
import io.runon.trading.CountryCode;

/**
 * 주식 캔들 관련 유틸성 클래스
 * @author macle
 */
public class StockSortTest {
    public static void main(String[] args) {
        String [] exchanges = {
                "KOSPI"
                , "KOSDAQ"
        };

        Stock[] stocks = Stocks.getStocks(exchanges);

        System.out.println(stocks[0]);
        System.out.println(stocks[1]);
        StockCandles.sortUseLastOpenTime(stocks, CountryCode.kOR, "1d");


        System.out.println("-------------------------");


        System.out.println(stocks[0]);
        System.out.println(stocks[1]);
    }
}