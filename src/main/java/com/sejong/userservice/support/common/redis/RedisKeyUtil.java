package com.sejong.userservice.support.common.redis;

import com.sejong.userservice.support.common.constants.PostType;

public class RedisKeyUtil {
    public static String likeCountKey(PostType postType, Long postId) {
        return "post:" + postType + ":" + postId + ":like:count";
    }
    
    public static String viewCountKey(PostType postType, Long postId) {
        return "post:" + postType + ":" + postId + ":view:count";
    }
    
    public static String viewIpKey(PostType postType, Long postId, String ip) {
        return "post:" + postType + ":" + postId + ":view:ip:" + ip;
    }
}