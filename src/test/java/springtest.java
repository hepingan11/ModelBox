import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
public class springtest {

    @Test
    public void test() {
        String endpoint = "http://sd.hepingan.top";
        String username = "";
        String password = "";

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(endpoint + "/sdapi/v1/txt2img");

        try {
            String credentials = username + ":" + password;
            String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            request.setHeader("Authorization", "Basic " + base64Credentials);

            String json = "{" +
                    "\"prompt\": \"lovely girly,avatar\"," +
                    "\"step\": 10," +
                    "\"height\": 512," +
                    "\"width\": 512," +
                    "\"override_settings\": {" +
                    "\"sd_model_checkpoint\": \"mixProV4.Cqhm.safetensors\"" +
                    "}" +
                    "}";
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 200) {
                if (responseEntity != null) {
                    String responseJson = EntityUtils.toString(responseEntity);
                    // 解析响应的图片数据并保存
                    // 注意使用不同的命名方式来保存不同的图片，这里默认保存为4.png
                    FileOutputStream fileOutputStream = new FileOutputStream("4.png");
                    String base64Image = responseJson.substring(responseJson.indexOf("\"images\"") + 10, responseJson.length()-3);
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image.getBytes());

                    fileOutputStream.write(imageBytes);
                    fileOutputStream.close();

                    // 打印响应结果
                    System.out.println(responseJson);
                }
            } else {
                System.out.println(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void processImages() throws IOException {

        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpPost httpPost =new HttpPost("https://sd.hepingan.top/sdapi/v1/txt2img");
        JSONObject param = new JSONObject();
        param.put("prompt", "girl");
        param.put("step",10);
        param.put("height",512);
        param.put("width",512);
        param.put("override_settings",new JSONObject().put("sd_model_checkpoint","majicMIX realistic_v6.safetensors"));

        StringEntity stringEntity=new StringEntity(param.toString());
        Header header=new BasicHeader("Content-Type","application/json");
        httpPost.setHeader(header);
        httpPost.setHeader("Authorization","Basic Og== ");
        httpPost.setHeader("Connection","keep-alive");
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse execute = aDefault.execute(httpPost);

        String s = EntityUtils.toString(execute.getEntity());
        System.out.println("结果为:"+s);

    }

    @Test
    public void processImages2() throws IOException {

    }
}