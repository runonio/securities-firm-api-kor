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

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * 파일처리 관련 유틸성 클래스
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

        updateAccessToken();

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

    }

    private final Object accessTokenLock = new Object();
    public void updateAccessToken(){
        synchronized (accessTokenLock) {
            if(accessToken != null && accessToken.isValid()){
                return;
            }

            HttpApiResponse httpResponse = HttpApis.postJson(domain + "/oauth2/tokenP", accessTokenParam);
            if(httpResponse.getResponseCode() != HttpURLConnection.HTTP_OK){
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
        map.put("authorization", accessToken.getAuthorization());
        map.put("appkey", key);
        map.put("appsecret", secretKey);
        map.put("custtype", customerType);
        return map;
    }

    public static String getDomain(boolean isActual){
        if(isActual){
            return ACTUAL_DOMAIN;
        }else{
            return SIMULATED_DOMAIN;
        }
    }

    public String getCandleJsonText(String symbol, String type, String interval, String beginYmd, String endYmd){
        //https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations#L_07802512-4f49-4486-91b4-1050b6f5dc9d
        String url = "/uapi/domestic-stock/v1/quotations/inquire-price";

        String query = "";

//
//        String param = "{\n" +
//                "            \"fid_cond_mrkt_div_code\": \"J\",\n" +
//                "            \"fid_input_date_1\": \"20220411\",\n" +
//                "            \"fid_input_date_2\": \"20220509\",\n" +
//                "            \"fid_input_iscd\": \"000660\",\n" +
//                "            \"fid_org_adj_prc\": \"0\",\n" +
//                "            \"fid_period_div_code\": \"D\"\n" +
//                "        }";
//

        return null;
    }

    public String getLastAccessTokenJson(){
        JsonObject lastToken  = jsonFileProperties.getJsonObject("last_access_token");
        if(lastToken == null){
            return null;
        }
        return GsonUtils.toJson(lastToken);
    }



    public static void main(String[] args) {
        KoreainvestmentApi api = new KoreainvestmentApi();
//        String text = api.getAccessTokenJsonText();
//        System.out.println(text);
    }




}
