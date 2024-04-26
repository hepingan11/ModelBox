import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@SpringBootTest
public class springtest {

    @Test
    public static void main(String[] args) {
        String endpoint = "http://sd.fc-stable-diffusion-plus.1398817069756357.cn-hangzhou.fc.devsapp.net";
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
}