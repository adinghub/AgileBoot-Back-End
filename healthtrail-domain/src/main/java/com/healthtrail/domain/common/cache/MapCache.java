package com.healthtrail.domain.common.cache;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.healthtrail.common.enums.common.BusinessTypeEnum;
import com.healthtrail.common.enums.common.GenderEnum;
import com.healthtrail.common.enums.common.LoginStatusEnum;
import com.healthtrail.common.enums.common.NoticeStatusEnum;
import com.healthtrail.common.enums.common.NoticeTypeEnum;
import com.healthtrail.common.enums.common.OperationStatusEnum;
import com.healthtrail.common.enums.common.StatusEnum;
import com.healthtrail.common.enums.common.UserStatusEnum;
import com.healthtrail.common.enums.common.YesOrNoEnum;
import com.healthtrail.common.enums.health.AppPushPlatformEnum;
import com.healthtrail.common.enums.health.HealthFollowUpTaskStatusEnum;
import com.healthtrail.common.enums.health.HealthReportItemAbnormalFlagEnum;
import com.healthtrail.common.enums.health.HealthReportParseStatusEnum;
import com.healthtrail.common.enums.health.MedicationReminderNotifyStatusEnum;
import com.healthtrail.common.enums.health.MedicationReminderStatusEnum;
import com.healthtrail.common.enums.dictionary.Dictionary;
import com.healthtrail.common.enums.DictionaryEnum;
import com.healthtrail.common.enums.dictionary.DictionaryData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 本地一级缓存  使用Map
 *
 * @author valarchie
 */
public class MapCache {

    /** 字典数据本地缓存，key 为字典名称，value 为字典项列表 */
    private static final Map<String, List<DictionaryData>> DICTIONARY_CACHE = MapUtil.newHashMap(128);

    private MapCache() {
    }

    static {
        initDictionaryCache();
    }

    private static void initDictionaryCache() {
        // TODO 这个可以做成自动扫描
        loadInCache(BusinessTypeEnum.values());
        loadInCache(YesOrNoEnum.values());
        loadInCache(StatusEnum.values());
        loadInCache(GenderEnum.values());
        loadInCache(NoticeStatusEnum.values());
        loadInCache(NoticeTypeEnum.values());
        loadInCache(OperationStatusEnum.values());
        loadInCache(LoginStatusEnum.values());
        loadInCache(UserStatusEnum.values());
        loadInCache(AppPushPlatformEnum.values());
        loadInCache(HealthFollowUpTaskStatusEnum.values());
        loadInCache(HealthReportItemAbnormalFlagEnum.values());
        loadInCache(HealthReportParseStatusEnum.values());
        loadInCache(MedicationReminderStatusEnum.values());
        loadInCache(MedicationReminderNotifyStatusEnum.values());
    }


    public static Map<String, List<DictionaryData>> dictionaryCache() {
        return DICTIONARY_CACHE;
    }

    private static void loadInCache(DictionaryEnum[] dictionaryEnums) {
        DICTIONARY_CACHE.put(getDictionaryName(dictionaryEnums[0].getClass()), arrayToList(dictionaryEnums));
    }


    private static String getDictionaryName(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        Dictionary annotation = clazz.getAnnotation(Dictionary.class);

        Objects.requireNonNull(annotation);
        return annotation.name();
    }

    @SuppressWarnings("rawtypes")
    private static List<DictionaryData> arrayToList(DictionaryEnum[] dictionaryEnums) {
        if(ArrayUtil.isEmpty(dictionaryEnums)) {
            return ListUtil.empty();
        }
        return Arrays.stream(dictionaryEnums).map(DictionaryData::new).collect(Collectors.toList());
    }


}
