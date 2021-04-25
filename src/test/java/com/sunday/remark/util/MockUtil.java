package com.sunday.remark.util;

import net.sf.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MockUtil {
    public static String performPostExpectOK(MockMvc mockMvc, String url, String jsonBody) throws Exception{
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    public static String performPost(MockMvc mockMvc, String url, String jsonBody) throws Exception{
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andReturn().getResponse().getContentAsString();
    }

    public static String getCode(String body){
        JSONObject object = JSONObject.fromObject(body);
        return object.getString("status");
    }

    public static String performDeleteExpectOK(MockMvc mockMvc, String url, String jsonBody) throws Exception{
        return mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

}
