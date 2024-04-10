package io.runon.stock.securities.firm.api.kor.koreainvestment;

import com.seomse.commons.config.Config;
import com.seomse.commons.utils.time.Times;
import com.seomse.commons.utils.time.YmdUtil;
import io.runon.stock.trading.Stock;
import io.runon.stock.trading.Stocks;
import io.runon.stock.trading.exception.StockDataException;
import io.runon.trading.CountryCode;
import io.runon.trading.TradingTimes;
import io.runon.trading.data.candle.CandleDataUtils;
import io.runon.trading.data.csv.CsvCandle;
import io.runon.trading.data.csv.CsvCandleOut;
import io.runon.trading.data.csv.CsvTimeFile;
import io.runon.trading.data.file.TimeName;
import io.runon.trading.technical.analysis.candle.TradeCandle;

import java.nio.file.FileSystems;

/**
 * 현물 일봉 캔들 내리기
 * 한국 투자증권은 아직 분봉과거치를 지원하지 않음
 * 올해 지원예정 중이라고 하였음
 * @author macle
 */
public class SpotDailyCandleOut {

    private final KoreainvestmentApi koreainvestmentApi;

    private long sleepTime ;

    public SpotDailyCandleOut(KoreainvestmentApi koreainvestmentApi){
        this.koreainvestmentApi = koreainvestmentApi;
        sleepTime =  Config.getLong("stock.securities.firm.api.kor.koreainvestment.candle.sleep.time", koreainvestmentApi.getSleepTime());
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void out(String beginYmd){
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

    public void out(Stock stock, String beginYmd){
        String type;
        if(stock.getStockType().startsWith("ETF")){
             type = "ETF";
        }else if(stock.getStockType().startsWith("ETN")){
            type = "ETN";
        }else if(stock.getStockType().startsWith("STOCK")){
            type = "J";
        }else{
            throw new StockDataException("unknown stock type: " + stock.getStockType());
        }

        String nowYmd = YmdUtil.now(TradingTimes.KOR_ZONE_ID);
        int nowYmdNum = Integer.parseInt(nowYmd);
        KoreainvestmentPeriodDataApi periodDataApi = koreainvestmentApi.getPeriodDataApi();

        String nextYmd = beginYmd;


        String fileSeparator = FileSystems.getDefault().getSeparator();

        String filesDirPath = CandleDataUtils.getStockSpotCandlePath(CountryCode.kOR)+fileSeparator+stock.getStockId()+fileSeparator+"1d";
        long lastOpenTime = CsvTimeFile.getLastOpenTime(filesDirPath);

        if(lastOpenTime > -1){
            String lastYmd = YmdUtil.getYmd(lastOpenTime, TradingTimes.KOR_ZONE_ID);
            if(YmdUtil.compare(lastYmd, nextYmd) > 0){
                nextYmd = lastYmd;
            }
        }

        TimeName.Type timeNameType = TimeName.getCandleType(Times.DAY_1);

        //최대100건
        for(;;){

            String endYmd = YmdUtil.getYmd(nextYmd, 100);

            int endYmdNum =  Integer.parseInt( endYmd);
            if(endYmdNum > nowYmdNum){
                endYmd = nowYmd;
            }

            String text = periodDataApi.getPeriodDataJsonText(stock.getSymbol(),type,"D", nextYmd, endYmd, true);
            TradeCandle [] candles = KoreainvestmentPeriodDataApi.getCandles(text);
            String [] lines = CsvCandle.lines(candles);

            CsvCandleOut.outBackPartChange(lines, filesDirPath, timeNameType, TradingTimes.KOR_ZONE_ID);

            if(endYmdNum >= nowYmdNum){
                break;
            }

            nextYmd = YmdUtil.getYmd(nextYmd, 1);

            try {
                Thread.sleep(sleepTime);
            }catch (Exception ignore){}

        }


    }




}
