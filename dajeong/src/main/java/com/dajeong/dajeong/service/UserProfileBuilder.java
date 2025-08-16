package com.dajeong.dajeong.service;

import com.dajeong.dajeong.entity.User;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserProfileBuilder {

    public Map<String, Object> buildFrom(User user) {
        Map<String, Object> m = new HashMap<>();

        m.put("name",        orEmpty(user.getName()));
        m.put("nationality", enumOrString(user.getNationality()));
        m.put("region",      enumOrString(user.getRegion()));
        
        m.put("age",         user.getAge() != null ? user.getAge() : 0);
        m.put("childAge",    user.getChildAge() != null ? user.getChildAge() : 0);
        m.put("married",     user.getMarried() != null ? user.getMarried() : false);
        m.put("hasChildren", user.getHasChildren() != null ? user.getHasChildren() : false);

        return m;
    }

    private String orEmpty(String v) {
        return v == null ? "" : v;
    }

    private String enumOrString(Object v) {
        if (v == null) return "";
        if (v instanceof String) return (String) v;
        if (v instanceof Enum<?>) {
            Enum<?> e = (Enum<?>) v;
            try {
                Method m = e.getClass().getMethod("getDescription");
                Object desc = m.invoke(e);
                if (desc != null) return desc.toString();
            } catch (Exception ignore) {
                // 메서드 없거나 호출 실패 시 name() 사용
            }
            return e.name();
        }
        return v.toString();
    }
}
