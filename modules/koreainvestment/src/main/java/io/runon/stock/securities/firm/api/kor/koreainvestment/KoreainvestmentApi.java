package io.runon.stock.securities.firm.api.kor.koreainvestment;

import com.google.gson.JsonObject;
import com.seomse.commons.config.Config;
import com.seomse.commons.config.JsonFileProperties;
import com.seomse.commons.config.JsonFilePropertiesManager;
import com.seomse.commons.exception.TokenException;
import com.seomse.commons.http.HttpApi;
import com.seomse.commons.http.HttpApiResponse;
import com.seomse.commons.http.HttpApis;
import com.seomse.commons.utils.GsonUtils;
import com.seomse.commons.utils.time.Times;
import io.runon.stock.securities.firm.api.kor.koreainvestment.exception.KoreainvestmentApiException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author macle
 */
public class KoreainvestmentApi {
    //실전 투자
    private static final String ACTUAL_DOMAIN = "https://openapi.koreainvestment.com:9443";
    
    //모의 투자
    private static final String SIMULATED_DOMAIN ="https://openapivts.koreainvestment.com:29443";

    private static final KoreainvestmentApi instance = new KoreainvestmentApi();

    public static KoreainvestmentApi getInstance(){
        return instance;
    }

    private final String key = Config.getConfig("stock.securities.firm.api.kor.koreainvestment.key");
    private final String secretKey = Config.getConfig("stock.securities.firm.api.kor.koreainvestment.secret.key");

    private final String customerType =  Config.getConfig("stock.securities.firm.api.kor.koreainvestment.customer.type", "P");

    private final JsonFileProperties jsonFileProperties;

    private AccessToken accessToken;

    private final String accessTokenParam;

    private final String domain;

    public final HttpApi httpGet, httpPost;

    public final HttpApi [] httpApis;

    public KoreainvestmentApi(){

        String jsonPropertiesName = "securities_firm_kor_koreainvestment.json";
        jsonFileProperties = JsonFilePropertiesManager.getInstance().getByName(jsonPropertiesName);

        boolean isActual = Config.getBoolean("stock.securities.firm.api.kor.koreainvestment.actual", true);
        domain = getDomain(isActual);

        JsonObject paramObject = new JsonObject();
        paramObject.addProperty("grant_type","client_credentials");
        paramObject.addProperty("appkey",key);
        paramObject.addProperty("appsecret", secretKey);
        accessTokenParam = GsonUtils.toJson(paramObject);

        JsonObject lastAccessTokenObj = jsonFileProperties.getJsonObject("last_access_token");
        if(lastAccessTokenObj != null){
            accessToken = new AccessToken(lastAccessTokenObj);
        }

        httpGet = new HttpApi();
        httpGet.setDefaultMethod("GET");
        httpGet.setReadTimeOut((int)Times.MINUTE_1);
        httpGet.setDefaultRequestProperty(makeRequestProperty());
        httpGet.setDefaultAddress(domain);

        httpPost = new HttpApi();
        httpPost.setDefaultMethod("POST");
        httpPost.setReadTimeOut((int)Times.MINUTE_1);
        httpPost.setDefaultRequestProperty(makeRequestProperty());
        httpPost.setDefaultAddress(domain);

        this.httpApis = new HttpApi[]{httpGet, httpPost};

        updateAccessToken();

    }

    private final Object accessTokenLock = new Object();
    public void updateAccessToken(){
        synchronized (accessTokenLock) {
            if(accessToken != null && accessToken.isValid()){
                return;
            }

            HttpApiResponse httpResponse = HttpApis.postJson(domain + "/oauth2/tokenP", accessTokenParam);
            if(httpResponse.getResponseCode() != 200){
                throw new TokenException("token make fail code:" + httpResponse.getResponseCode() +", " + httpResponse.getMessage());
            }

            JsonObject tokenObject = GsonUtils.fromJsonObject(httpResponse.getMessage());
            jsonFileProperties.set("last_access_token", tokenObject);

            accessToken = new AccessToken(tokenObject);

            String authorization = accessToken.getAuthorization();


            for(HttpApi httpApi : httpApis){
                httpApi.setRequestProperty("authorization", accessToken.getAuthorization());
            }
        }
    }

    public Map<String, String> makeRequestProperty(){
        Map<String, String> map = new HashMap<>();
        map.put("Content-Type","application/json; charset=utf-8");
        if(accessToken != null) {
            map.put("authorization", accessToken.getAuthorization());
        }
        map.put("appkey", key);
        map.put("appsecret", secretKey);
        map.put("custtype", customerType);
        //        map.put("personalSeckey", secretKey);
//        map.put("tr_cont", " ");
//        map.put("seq_no", " ");
//        map.put("mac_address", "");
//        map.put("phone_num", "");
//        map.put("ip_addr", "");
//        map.put("hashkey", "");
//        map.put("gt_uid", "");

        return map;
    }

    public static String getDomain(boolean isActual){
        if(isActual){
            return ACTUAL_DOMAIN;
        }else{
            return SIMULATED_DOMAIN;
        }
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

        updateAccessToken();
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

        HttpApiResponse response =  httpGet.getResponse(url + query, requestHeaderMap);
        if(response.getResponseCode() != 200){
            throw new KoreainvestmentApiException("token make fail code:" + response.getResponseCode() +", " + response.getMessage());
        }

        return response.getMessage();
    }

    public String getLastAccessTokenJson(){
        JsonObject lastToken  = jsonFileProperties.getJsonObject("last_access_token");
        if(lastToken == null){
            return null;
        }
        return GsonUtils.toJson(lastToken);
    }


    public static void main(String[] args)  {
        KoreainvestmentApi api = new KoreainvestmentApi();
        String text = api.getCandleJsonText("000660","J","D","20220411","20220509",true);
        System.out.println(text);

    }

}
