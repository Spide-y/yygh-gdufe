package com.gdufe.yygh.user.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.gdufe.yygh.common.helper.JwtHelper;
import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.model.user.UserInfo;
import com.gdufe.yygh.user.service.UserInfoService;
import com.gdufe.yygh.user.utils.ConstantWxPropertiesUtils;
import com.gdufe.yygh.user.utils.HttpClientUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//微信操作接口
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeiXinApiController {

    @Resource
    private UserInfoService userInfoService;

    //微信扫描后回调的方法
    @GetMapping("callback")
    public String callback(String code,String state){
        //1.获取临时票据 code
        System.out.println(code);
        //2.拿着code和微信id以及appscrect换取access_token
        //%s 占位符
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        //给占位符赋值
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWxPropertiesUtils.WX_OPEN_APP_ID,
                ConstantWxPropertiesUtils.WX_OPEN_APP_SECRET,
                code);

        //使用HttpClient请求这个地址
        try {
            String accessTokenInfo = HttpClientUtils.get(accessTokenUrl);
            System.out.println(accessTokenInfo);
            //从返回字符串中得到数据(利用json工具实现)
            JSONObject json = JSONObject.parseObject(accessTokenInfo);
            String access_token = json.getString("access_token");
            String openid = json.getString("openid");

            //判断数据库是否存在微信的扫面人信息
            //根据openid判断
            UserInfo user = userInfoService.selectWxInfoOpenId(openid);
            System.out.println("======"+user+"======");
            if (user == null){//数据库中不存在
                //拿着openid和access_token请求微信地址，得到扫码人信息
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
                String resultInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println(resultInfo);
                JSONObject userJson = JSONObject.parseObject(resultInfo);
                //解析用户信息
                //昵称
                String nickname = userJson.getString("nickname");
                //头像
                String headimgurl = userJson.getString("headimgurl");

                //把扫描人信息添加到数据库中
                user = new UserInfo();
                user.setNickName("笑脸");
                user.setOpenid(openid);
                user.setStatus(1);
                userInfoService.save(user);
            }



            //返回name和token的字符串
            Map<String, Object> map = new HashMap<>();
            String name = user.getName();
            if(StringUtils.isEmpty(name)) {
                name = user.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = user.getPhone();
            }
            map.put("name", name);
            //user中是否有手机号
            if(StringUtils.isEmpty(user.getPhone())) {
                map.put("openid", user.getOpenid());
            } else {
                map.put("openid", "");
            }
            String token = JwtHelper.createToken(user.getId(), name);
            map.put("token", token);

            //跳转到前端页面中
            return "redirect:" +
                    ConstantWxPropertiesUtils.YYGH_BASE_URL +
                    "/weixin/callback?token="+map.get("token")+"&openid="+map.get("openid")+"&name="
                    +URLEncoder.encode((String)map.get("name"),"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //生成微信扫描二维码
    //生成二维码需要的参数
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result genQrConnect(){
        try {
            Map<String,Object> map = new HashMap<>();
            map.put("appid", ConstantWxPropertiesUtils.WX_OPEN_APP_ID);
            map.put("scope","snsapi_login");
            String wxOpenRedirectUrl = ConstantWxPropertiesUtils.WX_OPEN_REDIRECT_URL;
            System.out.println(wxOpenRedirectUrl+"  "+ConstantWxPropertiesUtils.WX_OPEN_APP_ID+"  ");
            String uri = URLEncoder.encode(wxOpenRedirectUrl, "utf-8");
            map.put("redirect_uri",uri);
            map.put("state",System.currentTimeMillis()+"");
            return Result.ok(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

    }

    //回调方法,获得扫描人信息

}
