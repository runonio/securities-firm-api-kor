package io.runon.stock.securities.firm.api.kor.koreainvestment;

import java.util.HashMap;
import java.util.Map;
/**
 * 한국증권 url별 추가 헤더정보 관리
 * @author macle
 */
public class UrlAppendHeader {

    public final static Map<String, Map<String, String>> URL_REQUEST_PROPERTY_MAP = makeAppendRequestPropertyMap();

    public static Map<String, Map<String, String>> makeAppendRequestPropertyMap(){
        Map<String, Map<String, String>> map = new HashMap<>();

//        /uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice
        Map<String, String> inquireDailyItemchartprice = new HashMap<>();
        inquireDailyItemchartprice.put("tr_id","FHKST03010100");
        map.put("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice", inquireDailyItemchartprice);



        return map;
    }

    public static Map<String, String> getRequestPropertyMap(String url){
        return URL_REQUEST_PROPERTY_MAP.get(url);
    }

}