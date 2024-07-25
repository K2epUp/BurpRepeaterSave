package com.burp.save;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.logging.Logging;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.burp.save.ui.BurpUI;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class RepeaterSave implements HttpHandler {
    private final Logging logging;
    public static String filteHost = "";
    public static boolean pluginStatus = false;
    public static BurpUI burpUI = new BurpUI();

    public RepeaterSave(MontoyaApi api) {
        this.logging = api.logging();
    }

    public static JSONArray addrequest(String reqUrl, String httpPath, String host, String ipaddr, int port, String httpMethod, String protocol, JSONObject reqhead, String reqbody) {
        JSONObject jsonObject = new JSONObject(new LinkedHashMap<>());
        JSONArray jsonArray;
        if (burpUI.getTextArea().getText().startsWith("[") && burpUI.getTextArea().getText().endsWith("]")) {
            jsonArray = JSONObject.parseArray(burpUI.getTextArea().getText(), Feature.OrderedField);
        } else {
            jsonArray = new JSONArray();
        }
        jsonObject.put("SeparatorChars", "######################### REQUEST INFO #########################");
        jsonObject.put("url", reqUrl);
        jsonObject.put("path", httpPath);
        jsonObject.put("host", host);
        jsonObject.put("ip", ipaddr);
        jsonObject.put("port", port);
        jsonObject.put("method", httpMethod);
        jsonObject.put("protocol", protocol);
        jsonObject.put("headers", reqhead);
        jsonObject.put("body", reqbody);
        jsonArray.add(jsonObject);
        return jsonArray;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent httpRequestToBeSent) {

        String toolType = httpRequestToBeSent.toolSource().toolType().toolName();
        if (pluginStatus) {
            if (Objects.equals(toolType, "Repeater")) {
                String reqUrl = httpRequestToBeSent.url();
                String httpPath = httpRequestToBeSent.path();
                String host = httpRequestToBeSent.httpService().host();
                List<HttpHeader> headers = httpRequestToBeSent.headers();
                String ipaddr;
                if (!host.matches("[\\d.]+")) {
                    InetAddress inetAddress;
                    try {
                        inetAddress = InetAddress.getByName(host);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                    ipaddr = inetAddress.getHostAddress();
                } else {
                    ipaddr = host;
                }
                JSONObject headerJsonObject = new JSONObject(new LinkedHashMap<>());
                for (HttpHeader header : headers) {
                    headerJsonObject.put(header.name(), header.value());
                }
                int port = httpRequestToBeSent.httpService().port();
                String httpMethod = httpRequestToBeSent.method();
                String protocol = httpRequestToBeSent.httpService().secure() ? "https" : "http";
                if (filteHost.replaceAll(" ", "").length() == 0) {
                    if (httpRequestToBeSent.method().equals("POST")) {
                        postRequest(httpRequestToBeSent, reqUrl, httpPath, host, ipaddr, headerJsonObject, port, httpMethod, protocol);
                    } else if (httpRequestToBeSent.method().equals("GET")) {
                        getRequest(httpRequestToBeSent, reqUrl, httpPath, host, ipaddr, headerJsonObject, port, httpMethod, protocol);
                    }
                } else {
                    if (filteHost.replaceAll(" ", "").equals(httpRequestToBeSent.httpService().host())) {
                        if (httpRequestToBeSent.method().contains("POST")) {
                            postRequest(httpRequestToBeSent, reqUrl, httpPath, host, ipaddr, headerJsonObject, port, httpMethod, protocol);
                        } else if (httpRequestToBeSent.method().equals("GET")) {
                            getRequest(httpRequestToBeSent, reqUrl, httpPath, host, ipaddr, headerJsonObject, port, httpMethod, protocol);

                        }
                    }
                }
            }
        }
        return RequestToBeSentAction.continueWith(httpRequestToBeSent);
    }

    void postRequest(HttpRequestToBeSent httpRequestToBeSent, String reqUrl, String httpPath, String host, String ipaddr, JSONObject headerJsonObject, int port, String httpMethod, String protocol) {
        if (!burpUI.getTextArea().getText().contains(httpRequestToBeSent.bodyToString()) && !burpUI.getTextArea().getText().contains(httpRequestToBeSent.url())) {
            JSONArray jsonArray = addrequest(reqUrl, httpPath, host, ipaddr, port, httpMethod, protocol, headerJsonObject, httpRequestToBeSent.bodyToString());
            String jsonArrayString = JSONArray.toJSONString(jsonArray, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
            burpUI.getTextArea().setText(jsonArrayString);
            logging.logToOutput(jsonArrayString);
        }
    }

    private void getRequest(HttpRequestToBeSent httpRequestToBeSent, String reqUrl, String httpPath, String host, String ipaddr, JSONObject headerJsonObject, int port, String httpMethod, String protocol) {
        if (httpRequestToBeSent.path().contains("?")) {
//            String[] a = httpRequestToBeSent.path().split("[?]");
//            String b = a[0].replaceFirst("http[s]?://[\\w-_]+([.][\\w-_]+){1,10}", "");
            if (!burpUI.getTextArea().getText().contains(httpRequestToBeSent.url())) {
                JSONArray jsonArray = addrequest(reqUrl, httpPath, host, ipaddr, port, httpMethod, protocol, headerJsonObject, httpRequestToBeSent.bodyToString());
                String jsonArrayString = JSONArray.toJSONString(jsonArray, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
                burpUI.getTextArea().setText(jsonArrayString);
                logging.logToOutput(jsonArrayString);
            }
        }
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived httpResponseReceived) {
        return null;
    }
}
