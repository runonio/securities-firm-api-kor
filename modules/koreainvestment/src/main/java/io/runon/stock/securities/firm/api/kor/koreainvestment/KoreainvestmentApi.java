package io.runon.stock.securities.firm.api.kor.koreainvestment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.seomse.commons.config.Config;
import com.seomse.commons.config.JsonFileProperties;
import com.seomse.commons.config.JsonFilePropertiesManager;
import com.seomse.crawling.core.http.HttpOptionDataKey;
import com.seomse.crawling.core.http.HttpUrl;
import org.json.JSONObject;

/**
 * 파일처리 관련 유틸성 클래스
 * @author macle
 */
public class KoreainvestmentApi {
    //실전 투자
    private static final String ACTUAL_DOMAIL = "https://openapi.koreainvestment.com:9443";
    
    //모의 투자
    private static final String SIMULATED_DOMAIL ="https://openapivts.koreainvestment.com:29443";


    private static final KoreainvestmentApi instance = new KoreainvestmentApi();

    public static KoreainvestmentApi getInstance(){
        return instance;
    }


    private boolean isActual = Config.getBoolean("stock.securities.firm.api.kor.koreainvestment.actual", true);

    private String key = Config.getConfig("stock.securities.firm.api.kor.koreainvestment.key");
    private String secretkey = Config.getConfig("stock.securities.firm.api.kor.koreainvestment.secret.key");


    private final String jsonPropertiesName = "securities_firm_kor_koreainvestment.json";

    private long lastAccessTokenTime = -1L;
    private JsonObject lastAccessTokenObj = null;


    private final Gson gson = new Gson();

    private  final JsonFileProperties jsonFileProperties;

    public KoreainvestmentApi(){
        jsonFileProperties = JsonFilePropertiesManager.getInstance().getByName(jsonPropertiesName);
        lastAccessTokenObj = jsonFileProperties.getJsonObject("last_access_token");
        setLastAccessToken();
    }

    public void setLastAccessToken(String tokenJsonText){
        lastAccessTokenObj = gson.fromJson(tokenJsonText, JsonObject.class);
        setLastAccessToken();
    }

    public void setLastAccessToken(){
        if(lastAccessTokenObj == null){
            return;
        }
    }



    public String getTest(){
//        HttpUrl.get()

        JSONObject optionData = new JSONObject();
        optionData.put(HttpOptionDataKey.REQUEST_METHOD, "GET");
        optionData.put(HttpOptionDataKey.CHARACTER_SET,"UTF-8");


        JSONObject requestProperty = new JSONObject();
        requestProperty.put("content-type", "application/json; charset=utf-8");
        requestProperty.put("appkey", key);
        requestProperty.put("secretkey", secretkey);
        requestProperty.put("custtype","B");
        requestProperty.put("tr_id","FHKST03010100");

        optionData.put(HttpOptionDataKey.REQUEST_PROPERTY, requestProperty);

        String param = " {\n" +
                "            \"fid_cond_mrkt_div_code\": \"J\",\n" +
                "            \"fid_input_date_1\": \"20220411\",\n" +
                "            \"fid_input_date_2\": \"20220509\",\n" +
                "            \"fid_input_iscd\": \"000660\",\n" +
                "            \"fid_org_adj_prc\": \"0\",\n" +
                "            \"fid_period_div_code\": \"D\"\n" +
                "        }";

        optionData.put(HttpOptionDataKey.OUTPUT_STREAM_WRITE, param);

        return HttpUrl.getScript(getDomain() +"/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice" ,optionData);
    }

    public String getToken(){

        JSONObject optionData = new JSONObject();
        optionData.put(HttpOptionDataKey.REQUEST_METHOD, "POST");
        optionData.put(HttpOptionDataKey.CHARACTER_SET,"UTF-8");


        JSONObject requestProperty = new JSONObject();
        requestProperty.put("content-type", "application/json; charset=utf-8");
        requestProperty.put("grant_type", "client_credentials");
        requestProperty.put("appkey", key);
        requestProperty.put("secretkey", secretkey);

        optionData.put(HttpOptionDataKey.REQUEST_PROPERTY, requestProperty);


        return HttpUrl.getScript(getDomain() +"/oauth2/tokenP" ,optionData);
    }


    public String getDomain(){
        if(isActual){
            return ACTUAL_DOMAIL;
        }else{
            return SIMULATED_DOMAIL;
        }
    }


    public static void main(String[] args) {


       String test =  KoreainvestmentApi.getInstance().getTest();
       System.out.println(test);
    }


}
