package example.price;

import io.runon.stock.securities.firm.api.kor.koreainvestment.KoreainvestmentApi;

/**
 * 기간별 가격 데이터와 정보
 * 한국투자증권 오리지날 데이터
 * @author macle
 */
public class PricePeriodJsonTextOriginal {
    public static void main(String[] args) {
        KoreainvestmentApi api = new KoreainvestmentApi();
        String text = api.getPeriodDataApi().getPeriodDataJsonText("000660","J","D","20220411","20220509",true);
        System.out.println(text);
    }
}
