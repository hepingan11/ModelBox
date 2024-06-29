import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.constants.ServerConstant;
import com.cn.bdth.enums.FileEnum;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


public class ImgTest {
    public static void main(String[] args)  {
        final RedisUtils redisUtils = null;
        final String value = redisUtils.getValue(ServerConstant.SD_CONFIG).toString();
        System.out.println(value);
        System.out.println(value.toString());
        List<String> list = List.of(value.toString(),"/sdapi/v1");
        final String join = String.join("", list);
        System.out.println(join);

//        LocalDateTime localDateTime = LocalDateTime.now();
//        System.out.println(localDateTime);
//        String name =  LocalDateTime.now().toString().replace(":","-").replace(".","-");
//        System.out.println(name);
//        CloseableHttpClient aDefault = HttpClients.createDefault();
//
//        HttpPost httpPost =new HttpPost("https://sd.hepingan.top/sdapi/v1/txt2img");
//        JSONObject param = new JSONObject();
//        param.put("prompt", "girl");
//        param.put("step",10);
//        param.put("height",512);
//        param.put("width",512);
//        param.put("override_settings",new JSONObject().put("sd_model_checkpoint","majicMIX realistic_v6.safetensors"));
//
//        StringEntity stringEntity=new StringEntity(param.toString());
//        Header header=new BasicHeader("Content-Type","application/json");
//        httpPost.setHeader(header);
//        httpPost.setHeader("Authorization","Basic Og== ");
//        httpPost.setHeader("Connection","keep-alive");
//        httpPost.setEntity(stringEntity);
//        CloseableHttpResponse execute = aDefault.execute(httpPost);
//
////        System.out.println(execute);
//        String s = EntityUtils.toString(execute.getEntity());
//
//        System.out.println(s);
//        AliUploadUtils aliUploadUtils = new AliUploadUtils();
//
//        String imageUri = aliUploadUtils.uploadBase64(Objects.requireNonNull(JSONObject.parseObject(s)).getJSONArray("images").getString(0), FileEnum.PAINTING.getDec());
//
//        System.out.println(imageUri);
    }
}
