package io.runon.stock.securities.firm.api.kor.koreainvestment;

import io.runon.stock.trading.Stock;
import io.runon.stock.trading.Stocks;

/**
 * 현물 일봉 캔들 내리기
 * 한국 투자증권은 아직 분봉과거치를 지원하지 않음
 * 올해 지원예정 중이라고 하였음
 * @author macle
 */
public class SpotDailyCandleOut {


    public static void out(String beginYmd){
        //전체 종목 일봉 내리기
        //KONEX 는 제외
        String [] exchanges = {
                "KOSPI"
                , "KOSDAQ"
        };

        Stock [] stocks = Stocks.getStocks(exchanges);
        for(Stock stock : stocks){
            out(stock, beginYmd);
        }
    }


    public static void out(Stock stock, String beginYmd){
        
    }




}
