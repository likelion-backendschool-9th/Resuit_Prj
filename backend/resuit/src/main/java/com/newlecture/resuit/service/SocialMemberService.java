package com.newlecture.resuit.service;

import java.util.Map;

public interface SocialMemberService {
    String resolve(String provider, Map<String, Object> attributes);
}
