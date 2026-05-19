package com.healthtrail.common.utils.ip;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * IP地理位置信息模型，包含国家、区域、省份、城市和ISP等信息
 *
 * @author valarchie
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IpRegion {
    private static final String UNKNOWN = "未知";
    /**
     * 国家
     */
    private String country;
    /**
     * 区域
     */
    private String region;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 网络运营商
     */
    private String isp;

    public IpRegion(String province, String city) {
        this.province = province;
        this.city = city;
    }

    public String briefLocation() {
       return String.format("%s %s",
           CharSequenceUtil.nullToDefault(province, UNKNOWN),
           CharSequenceUtil.nullToDefault(city, UNKNOWN)).trim();
    }

}
