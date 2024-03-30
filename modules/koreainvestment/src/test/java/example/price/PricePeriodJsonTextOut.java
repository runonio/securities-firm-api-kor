package example.price;

import io.runon.stock.securities.firm.api.kor.koreainvestment.KoreainvestmentApi;
import io.runon.stock.securities.firm.api.kor.koreainvestment.KoreainvestmentPeriodDataApi;
import io.runon.trading.data.csv.CsvCandle;
import io.runon.trading.technical.analysis.candle.TradeCandle;

/**
 * 기간별 가격 데이터와 정보
 * 한국투자증권 오리지날 데이터
 * @author macle
 */
public class PricePeriodJsonTextOut {
    public static void main(String[] args) {
        KoreainvestmentApi api = new KoreainvestmentApi();

        KoreainvestmentPeriodDataApi periodDataApi = api.getPeriodDataApi();

        String text = periodDataApi.getPeriodDataJsonText("000660","J","D","20220411","20220509",true);

        TradeCandle [] candles = periodDataApi.getCandles(text);


        String candleCsv = CsvCandle.value(candles[0]);

        System.out.println(candleCsv);

        System.out.println(candles[0]);
        System.out.println(CsvCandle.make(candleCsv));



    }
}
