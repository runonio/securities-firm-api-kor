package io.runon.stock.securities.firm.api.kor.koreainvestment;

import com.seomse.commons.http.HttpApiResponse;
import com.seomse.commons.utils.time.YmdUtil;
import io.runon.stock.securities.firm.api.kor.koreainvestment.exception.KoreainvestmentApiException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 한국투자증권 시장 관련 API 정의
 * API가 많아서 정리한 클래스를 나눈다.
 * @author macle
 */
public class KoreainvestmentMarketApi {
    private final KoreainvestmentApi koreainvestmentApi;
    public KoreainvestmentMarketApi(KoreainvestmentApi koreainvestmentApi){
        this.koreainvestmentApi = koreainvestmentApi;
    }

    /**
     *
     */
    public String getClosedDaysJson(String baseYmd){
        koreainvestmentApi.updateAccessToken();
        String url = "/uapi/domestic-stock/v1/quotations/chk-holiday";
        Map<String, String> requestHeaderMap = koreainvestmentApi.computeIfAbsenttPropertySingleMap(url,"tr_id","CTCA0903R");

        String query = "?BASS_DT="+ baseYmd +"&CTX_AREA_NK=&CTX_AREA_FK=" ;
        HttpApiResponse response =  koreainvestmentApi.getHttpGet().getResponse(url + query, requestHeaderMap);
        if(response.getResponseCode() != 200){
            throw new KoreainvestmentApiException("token make fail code:" + response.getResponseCode() +", " + response.getMessage());
        }

        return response.getMessage();
    }

    public String [] getClosedDays(String beginYmd, String endYmd){

        List<String> list = new ArrayList<>();

        String baseYmd = beginYmd;

        outer:
        for(;;){
            String json = getClosedDaysJson(baseYmd);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = jsonObject.getJSONArray("output");
            for (int i = 0; i < array.length(); i++) {

                JSONObject row = array.getJSONObject(i);
                if(row.isNull("bass_dt")){
                    break outer;
                }

                String ymd = row.getString("bass_dt");
                if(YmdUtil.compare(ymd, beginYmd) < 0){
                    baseYmd = ymd;
                    continue ;
                }

                int compare = YmdUtil.compare(ymd, endYmd);

                if(compare >  0 ){
                    break outer;
                }

                boolean isClosed = row.getString("opnd_yn").equalsIgnoreCase("N");
                if(isClosed){
                    list.add(ymd);
                }

                baseYmd = ymd;
                if(compare ==  0 ){
                    break outer;
                }
                //너무 잦은 호출을 방지하기위한 조건
                koreainvestmentApi.sleep();
            }
        }

        String [] days = list.toArray(new String[0]);
        list.clear();

        return days;
    }


    public static void main(String[] args) {
        KoreainvestmentApi api = new KoreainvestmentApi();

        KoreainvestmentMarketApi marketApi = api.getMarketApi();
//        System.out.println(marketApi.getClosedDaysJson("2022"));


        String [] closeDays = marketApi.getClosedDays("20240410" , "20240431");
        for(String day : closeDays){
            System.out.println(day);
        }
    }

}