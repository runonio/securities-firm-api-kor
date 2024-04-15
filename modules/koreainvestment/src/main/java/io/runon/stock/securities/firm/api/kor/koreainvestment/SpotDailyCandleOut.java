package io.runon.stock.securities.firm.api.kor.koreainvestment;

import com.seomse.commons.config.Config;
import com.seomse.commons.utils.time.Times;
import com.seomse.commons.utils.time.YmdUtil;
import io.runon.stock.trading.Stock;
import io.runon.stock.trading.Stocks;
import io.runon.trading.CountryCode;
import io.runon.trading.TradingTimes;
import io.runon.trading.data.candle.CandleDataUtils;
import io.runon.trading.data.csv.CsvCandle;
import io.runon.trading.data.csv.CsvCandleOut;
import io.runon.trading.data.csv.CsvTimeFile;
import io.runon.trading.data.file.TimeName;
import io.runon.trading.technical.analysis.candle.Candles;
import io.runon.trading.technical.analysis.candle.TradeCandle;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.FileSystems;

/**
 * 현물 일봉 캔들 내리기
 * 한국 투자증권은 아직 분봉과거치를 지원하지 않음
 * 올해 지원예정 중이라고 하였음
 * @author macle
 */
@Slf4j
public class SpotDailyCandleOut {

    private final KoreainvestmentApi koreainvestmentApi;



    public SpotDailyCandleOut(KoreainvestmentApi koreainvestmentApi){
        this.koreainvestmentApi = koreainvestmentApi;

    }

    public SpotDailyCandleOut(){
        this.koreainvestmentApi = KoreainvestmentApi.getInstance();
    }

    public void outKor(){
        //전체 종목 일봉 내리기
        //KONEX 는 제외
        String [] exchanges = {
                "KOSPI"
                , "KOSDAQ"
        };

        Stock [] stocks = Stocks.getStocks(exchanges);
        for(Stock stock : stocks){
            out(stock,  CountryCode.kOR);
        }
    }


    /**
     * 상장 시점부터 내릴 수 있는 전체 정보를 내린다.
     * @param stock 종목정보
     * @param countryCode 국가코드
     */
    public void out(Stock stock,  CountryCode countryCode){
//        String type;
//        if(stock.getStockType().startsWith("ETF")){
//             type = "ETF";
//        }else if(stock.getStockType().startsWith("ETN")){
//            type = "ETN";
//        }else if(stock.getStockType().startsWith("STOCK")){
//            type = "J";
//        }else{
//            throw new StockDataException("unknown stock type: " + stock.getStockType());
//        }

        String nowYmd = YmdUtil.now(TradingTimes.KOR_ZONE_ID);
        int nowYmdNum = Integer.parseInt(nowYmd);
        KoreainvestmentPeriodDataApi periodDataApi = koreainvestmentApi.getPeriodDataApi();

        
        //초기 데이터는 상장 년원일
        String nextYmd ;

        String fileSeparator = FileSystems.getDefault().getSeparator();

        String filesDirPath = CandleDataUtils.getStockSpotCandlePath(countryCode)+fileSeparator+stock.getStockId()+fileSeparator+"1d";



        long lastOpenTime = CsvTimeFile.getLastOpenTime(filesDirPath);

        if(lastOpenTime > -1){
            nextYmd = YmdUtil.getYmd(lastOpenTime, TradingTimes.KOR_ZONE_ID);
        }else{
            if(stock.getListedYmd() == null){
                log.error("listed ymd null: " + stock);
                return ;
            }
            nextYmd = Integer.toString(stock.getListedYmd());
        }

        TimeName.Type timeNameType = TimeName.getCandleType(Times.DAY_1);

        boolean isFirst = true;


        log.debug("start stock: " + stock);
        //최대100건
        for(;;){

            if(YmdUtil.compare(nextYmd, nowYmd) > 0){
                break;
            }

            String endYmd = YmdUtil.getYmd(nextYmd, 100);

            int endYmdNum =  Integer.parseInt( endYmd);
            if(endYmdNum > nowYmdNum){
                endYmd = nowYmd;
            }

            String text = periodDataApi.getPeriodDataJsonText(stock.getSymbol(),"D", nextYmd, endYmd, true);
            TradeCandle [] candles = KoreainvestmentPeriodDataApi.getCandles(text);
            String [] lines = CsvCandle.lines(candles);

            if(isFirst) {
                CsvCandleOut.outBackPartChange(lines, filesDirPath, timeNameType, TradingTimes.KOR_ZONE_ID);
                isFirst = false;
            }else{
                CsvCandleOut.outNewLines(lines, filesDirPath, timeNameType, TradingTimes.KOR_ZONE_ID);
            }

            if(endYmdNum >= nowYmdNum){
                break;
            }

            if(candles.length == 0){
                nextYmd = YmdUtil.getYmd(endYmd, 1);
            }else{
                nextYmd = YmdUtil.getYmd(Candles.getMaxYmd(candles, TradingTimes.KOR_ZONE_ID),1);
            }

            koreainvestmentApi.candleOutSleep();
        }

    }

}
