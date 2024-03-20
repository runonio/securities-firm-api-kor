package io.runon.stock.securities.firm.api.kor.koreainvestment;

import com.seomse.commons.http.HttpApiResponse;
import io.runon.stock.securities.firm.api.kor.koreainvestment.exception.KoreainvestmentApiException;

import java.util.Map;

/**
 * 한국투자증권 기간별 데이터 관련 API 정의
 * API가 많아서 정리한 클래스를 나눈다.
 * @author macle
 */
public class KoreainvestmentPeriodDataApi {
    private final KoreainvestmentApi koreainvestmentApi;
    public KoreainvestmentPeriodDataApi(KoreainvestmentApi koreainvestmentApi){
        this.koreainvestmentApi = koreainvestmentApi;
    }

    /**
     *
     * @param symbol 종목코드
     * @param type 시장유형 J : 주식, ETF, ETN
     * @param period 기간유형 	D:일봉, W:주봉, M:월봉, Y:년봉
     * @param beginYmd 시작년월일
     * @param endYmd 끝 년월일
     * @param isRevisePrice 수정주가 여뷰
     * @return 결과값 jsontext
     */
    public String getCandleJsonText(String symbol, String type, String period, String beginYmd, String endYmd, boolean isRevisePrice){
        //https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations#L_a08c3421-e50f-4f24-b1fe-64c12f723c77

        koreainvestmentApi.updateAccessToken();
        String url = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
        Map<String, String> requestHeaderMap = UrlAppendHeader.getRequestPropertyMap(url);

        //수정주가여부
        String sendRevisePrice;
        if(isRevisePrice){
            sendRevisePrice = "0";
        }else{
            sendRevisePrice = "1";
        }

        String query = "?fid_cond_mrkt_div_code="+ type +"&fid_input_iscd=" + symbol +"&fid_input_date_1=" + beginYmd +"&fid_input_date_2=" +endYmd +"&fid_period_div_code=" + period + "&fid_org_adj_prc=" + sendRevisePrice;

        HttpApiResponse response =  koreainvestmentApi.getHttpGet().getResponse(url + query, requestHeaderMap);
        if(response.getResponseCode() != 200){
            throw new KoreainvestmentApiException("token make fail code:" + response.getResponseCode() +", " + response.getMessage());
        }

        return response.getMessage();
    }
}
