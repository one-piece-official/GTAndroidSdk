// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: gt_ssp_config.proto
package com.gt.sdk.base.models;

import android.os.Parcelable;

import com.czhj.wire.AndroidMessage;
import com.czhj.wire.FieldEncoding;
import com.czhj.wire.Message;
import com.czhj.wire.ProtoAdapter;
import com.czhj.wire.ProtoReader;
import com.czhj.wire.ProtoWriter;
import com.czhj.wire.WireField;
import com.czhj.wire.internal.Internal;
import com.czhj.wire.okio.ByteString;

import java.io.IOException;
import java.util.List;

public final class Common extends AndroidMessage<Common, Common.Builder> {

    public static final ProtoAdapter<Common> ADAPTER = new ProtoAdapter_Common();

    public static final Parcelable.Creator<Common> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final Integer DEFAULT_CONFIGREFRESH = 0;

    public static final Boolean DEFAULT_IS_GDPR_REGION = false;

    public static final Integer DEFAULT_TRACKING_EXPIRATION_TIME = 0;

    public static final Integer DEFAULT_TRACKING_RETRY_INTERVAL = 0;

    public static final Integer DEFAULT_MAX_SEND_LOG_RECORDS = 100;

    public static final Integer DEFAULT_SEND_LOG_INTERVAL = 3;

    public static final Boolean DEFAULT_ENABLE_DEBUG_LEVEL = false;

    public static final Integer DEFAULT_LOAD_INTERVAL = 0;

    public static final Boolean DEFAULT_DISABLE_UP_LOCATION = false;

    public static final Boolean DEFAULT_LOG_ENC = false;

    /**
     * config刷新间隔
     */
    @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer configRefresh;

    /**
     * 是否gdpr地区，默认是false
     */
    @WireField(tag = 2, adapter = "com.squareup.wire.ProtoAdapter#BOOL")
    public final Boolean is_gdpr_region;

    /**
     * track事件过期时间，过期后不执行补发操作，单位秒
     */
    @WireField(tag = 3, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer tracking_expiration_time;

    /**
     * track上报失败的重发时间间隔
     */
    @WireField(tag = 4, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer tracking_retry_interval;

    /**
     * 每次上传打点日志的最大条数
     */
    @WireField(tag = 5, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer max_send_log_records;

    /**
     * 打点日志上报时间间隔（单位：秒）
     */
    @WireField(tag = 6, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer send_log_interval;

    /**
     * 点号位黑明单，默认会将100，101号点自动加入黑名单，如果下发了黑明单，以下发为准。
     */
    @WireField(tag = 7, adapter = "com.squareup.wire.ProtoAdapter#UINT32", label = WireField.Label.REPEATED)
    public final List<Integer> dc_log_blacklist;

    /**
     * 是否开启debug日志、初始化监测广告插件、监测结果打点和日志输出
     */
    @WireField(tag = 8, adapter = "com.squareup.wire.ProtoAdapter#BOOL")
    public final Boolean enable_debug_level;

    /**
     * 重复发起请求的间隔时间，在非ready的情况下，用来防止重复发起请求
     */
    @WireField(tag = 9, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer load_interval;

    /**
     * 是否禁止上传用户的位置信息。false: 不禁止上传；true: 禁止上传
     */
    @WireField(tag = 10, adapter = "com.squareup.wire.ProtoAdapter#BOOL")
    public final Boolean disable_up_location;

    /**
     * false: 不加密；true: 加密
     */
    @WireField(tag = 11, adapter = "com.squareup.wire.ProtoAdapter#BOOL")
    public final Boolean log_enc;

    /**
     * api配置
     */
    @WireField(tag = 12, adapter = "com.gt.sdk.base.models.UrlConfig#ADAPTER")
    public final UrlConfig urlConfig;

    /**
     * 激励视频配置
     */
    @WireField(tag = 13, adapter = "com.gt.sdk.base.models.RvConfig#ADAPTER")
    public final RvConfig rv_config;

    /**
     * 开屏配置
     */
    @WireField(tag = 14, adapter = "com.gt.sdk.base.models.SplashConfig#ADAPTER")
    public final SplashConfig splash_config;

    @WireField(tag = 15, adapter = "com.gt.sdk.base.models.NativeConfig#ADAPTER")
    public final NativeConfig native_config;

    public Common(Integer configRefresh, Boolean is_gdpr_region, Integer tracking_expiration_time, Integer tracking_retry_interval, Integer max_send_log_records, Integer send_log_interval, List<Integer> dc_log_blacklist, Boolean enable_debug_level, Integer load_interval, Boolean disable_up_location, Boolean log_enc, UrlConfig urlConfig, RvConfig rv_config, SplashConfig splash_config, NativeConfig native_config) {
        this(configRefresh, is_gdpr_region, tracking_expiration_time, tracking_retry_interval, max_send_log_records, send_log_interval, dc_log_blacklist, enable_debug_level, load_interval, disable_up_location, log_enc, urlConfig, rv_config, splash_config, native_config, ByteString.EMPTY);
    }

    public Common(Integer configRefresh, Boolean is_gdpr_region, Integer tracking_expiration_time, Integer tracking_retry_interval, Integer max_send_log_records, Integer send_log_interval, List<Integer> dc_log_blacklist, Boolean enable_debug_level, Integer load_interval, Boolean disable_up_location, Boolean log_enc, UrlConfig urlConfig, RvConfig rv_config, SplashConfig splash_config, NativeConfig native_config, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.configRefresh = configRefresh;
        this.is_gdpr_region = is_gdpr_region;
        this.tracking_expiration_time = tracking_expiration_time;
        this.tracking_retry_interval = tracking_retry_interval;
        this.max_send_log_records = max_send_log_records;
        this.send_log_interval = send_log_interval;
        this.dc_log_blacklist = Internal.immutableCopyOf("dc_log_blacklist", dc_log_blacklist);
        this.enable_debug_level = enable_debug_level;
        this.load_interval = load_interval;
        this.disable_up_location = disable_up_location;
        this.log_enc = log_enc;
        this.urlConfig = urlConfig;
        this.rv_config = rv_config;
        this.splash_config = splash_config;
        this.native_config = native_config;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.configRefresh = configRefresh;
        builder.is_gdpr_region = is_gdpr_region;
        builder.tracking_expiration_time = tracking_expiration_time;
        builder.tracking_retry_interval = tracking_retry_interval;
        builder.max_send_log_records = max_send_log_records;
        builder.send_log_interval = send_log_interval;
        builder.dc_log_blacklist = Internal.copyOf("dc_log_blacklist", dc_log_blacklist);
        builder.enable_debug_level = enable_debug_level;
        builder.load_interval = load_interval;
        builder.disable_up_location = disable_up_location;
        builder.log_enc = log_enc;
        builder.urlConfig = urlConfig;
        builder.rv_config = rv_config;
        builder.splash_config = splash_config;
        builder.native_config = native_config;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Common)) return false;
        Common o = (Common) other;
        return unknownFields().equals(o.unknownFields()) && Internal.equals(configRefresh, o.configRefresh) && Internal.equals(is_gdpr_region, o.is_gdpr_region) && Internal.equals(tracking_expiration_time, o.tracking_expiration_time) && Internal.equals(tracking_retry_interval, o.tracking_retry_interval) && Internal.equals(max_send_log_records, o.max_send_log_records) && Internal.equals(send_log_interval, o.send_log_interval) && dc_log_blacklist.equals(o.dc_log_blacklist) && Internal.equals(enable_debug_level, o.enable_debug_level) && Internal.equals(load_interval, o.load_interval) && Internal.equals(disable_up_location, o.disable_up_location) && Internal.equals(log_enc, o.log_enc) && Internal.equals(urlConfig, o.urlConfig) && Internal.equals(rv_config, o.rv_config) && Internal.equals(splash_config, o.splash_config) && Internal.equals(native_config, o.native_config);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (configRefresh != null ? configRefresh.hashCode() : 0);
            result = result * 37 + (is_gdpr_region != null ? is_gdpr_region.hashCode() : 0);
            result = result * 37 + (tracking_expiration_time != null ? tracking_expiration_time.hashCode() : 0);
            result = result * 37 + (tracking_retry_interval != null ? tracking_retry_interval.hashCode() : 0);
            result = result * 37 + (max_send_log_records != null ? max_send_log_records.hashCode() : 0);
            result = result * 37 + (send_log_interval != null ? send_log_interval.hashCode() : 0);
            result = result * 37 + dc_log_blacklist.hashCode();
            result = result * 37 + (enable_debug_level != null ? enable_debug_level.hashCode() : 0);
            result = result * 37 + (load_interval != null ? load_interval.hashCode() : 0);
            result = result * 37 + (disable_up_location != null ? disable_up_location.hashCode() : 0);
            result = result * 37 + (log_enc != null ? log_enc.hashCode() : 0);
            result = result * 37 + (urlConfig != null ? urlConfig.hashCode() : 0);
            result = result * 37 + (rv_config != null ? rv_config.hashCode() : 0);
            result = result * 37 + (splash_config != null ? splash_config.hashCode() : 0);
            result = result * 37 + (native_config != null ? native_config.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (configRefresh != null) builder.append(", configRefresh=").append(configRefresh);
        if (is_gdpr_region != null) builder.append(", is_gdpr_region=").append(is_gdpr_region);
        if (tracking_expiration_time != null)
            builder.append(", tracking_expiration_time=").append(tracking_expiration_time);
        if (tracking_retry_interval != null)
            builder.append(", tracking_retry_interval=").append(tracking_retry_interval);
        if (max_send_log_records != null)
            builder.append(", max_send_log_records=").append(max_send_log_records);
        if (send_log_interval != null)
            builder.append(", send_log_interval=").append(send_log_interval);
        if (!dc_log_blacklist.isEmpty())
            builder.append(", dc_log_blacklist=").append(dc_log_blacklist);
        if (enable_debug_level != null)
            builder.append(", enable_debug_level=").append(enable_debug_level);
        if (load_interval != null) builder.append(", load_interval=").append(load_interval);
        if (disable_up_location != null)
            builder.append(", disable_up_location=").append(disable_up_location);
        if (log_enc != null) builder.append(", log_enc=").append(log_enc);
        if (urlConfig != null) builder.append(", urlConfig=").append(urlConfig);
        if (rv_config != null) builder.append(", rv_config=").append(rv_config);
        if (splash_config != null) builder.append(", splash_config=").append(splash_config);
        if (native_config != null) builder.append(", native_config=").append(native_config);
        return builder.replace(0, 2, "Common{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<Common, Builder> {
        public Integer configRefresh = DEFAULT_CONFIGREFRESH;

        public Boolean is_gdpr_region = DEFAULT_IS_GDPR_REGION;

        public Integer tracking_expiration_time = DEFAULT_TRACKING_EXPIRATION_TIME;

        public Integer tracking_retry_interval = DEFAULT_TRACKING_RETRY_INTERVAL;

        public Integer max_send_log_records = DEFAULT_MAX_SEND_LOG_RECORDS;

        public Integer send_log_interval = DEFAULT_SEND_LOG_INTERVAL;

        public List<Integer> dc_log_blacklist;

        public Boolean enable_debug_level = DEFAULT_ENABLE_DEBUG_LEVEL;

        public Integer load_interval = DEFAULT_LOAD_INTERVAL;

        public Boolean disable_up_location = DEFAULT_DISABLE_UP_LOCATION;

        public Boolean log_enc = DEFAULT_LOG_ENC;

        public UrlConfig urlConfig;

        public RvConfig rv_config;

        public SplashConfig splash_config;

        public NativeConfig native_config;

        public Builder() {
            dc_log_blacklist = Internal.newMutableList();
        }

        /**
         * config刷新间隔
         */
        public Builder configRefresh(Integer configRefresh) {
            this.configRefresh = configRefresh;
            return this;
        }

        /**
         * 是否gdpr地区，默认是false
         */
        public Builder is_gdpr_region(Boolean is_gdpr_region) {
            this.is_gdpr_region = is_gdpr_region;
            return this;
        }

        /**
         * track事件过期时间，过期后不执行补发操作，单位秒
         */
        public Builder tracking_expiration_time(Integer tracking_expiration_time) {
            this.tracking_expiration_time = tracking_expiration_time;
            return this;
        }

        /**
         * track上报失败的重发时间间隔
         */
        public Builder tracking_retry_interval(Integer tracking_retry_interval) {
            this.tracking_retry_interval = tracking_retry_interval;
            return this;
        }

        /**
         * 每次上传打点日志的最大条数
         */
        public Builder max_send_log_records(Integer max_send_log_records) {
            this.max_send_log_records = max_send_log_records;
            return this;
        }

        /**
         * 打点日志上报时间间隔（单位：秒）
         */
        public Builder send_log_interval(Integer send_log_interval) {
            this.send_log_interval = send_log_interval;
            return this;
        }

        /**
         * 点号位黑明单，默认会将100，101号点自动加入黑名单，如果下发了黑明单，以下发为准。
         */
        public Builder dc_log_blacklist(List<Integer> dc_log_blacklist) {
            Internal.checkElementsNotNull(dc_log_blacklist);
            this.dc_log_blacklist = dc_log_blacklist;
            return this;
        }

        /**
         * 是否开启debug日志、初始化监测广告插件、监测结果打点和日志输出
         */
        public Builder enable_debug_level(Boolean enable_debug_level) {
            this.enable_debug_level = enable_debug_level;
            return this;
        }

        /**
         * 重复发起请求的间隔时间，在非ready的情况下，用来防止重复发起请求
         */
        public Builder load_interval(Integer load_interval) {
            this.load_interval = load_interval;
            return this;
        }

        /**
         * 是否禁止上传用户的位置信息。false: 不禁止上传；true: 禁止上传
         */
        public Builder disable_up_location(Boolean disable_up_location) {
            this.disable_up_location = disable_up_location;
            return this;
        }

        /**
         * false: 不加密；true: 加密
         */
        public Builder log_enc(Boolean log_enc) {
            this.log_enc = log_enc;
            return this;
        }

        /**
         * api配置
         */
        public Builder urlConfig(UrlConfig urlConfig) {
            this.urlConfig = urlConfig;
            return this;
        }

        /**
         * 激励视频配置
         */
        public Builder rv_config(RvConfig rv_config) {
            this.rv_config = rv_config;
            return this;
        }

        /**
         * 开屏配置
         */
        public Builder splash_config(SplashConfig splash_config) {
            this.splash_config = splash_config;
            return this;
        }

        public Builder native_config(NativeConfig native_config) {
            this.native_config = native_config;
            return this;
        }

        @Override
        public Common build() {
            return new Common(configRefresh, is_gdpr_region, tracking_expiration_time, tracking_retry_interval, max_send_log_records, send_log_interval, dc_log_blacklist, enable_debug_level, load_interval, disable_up_location, log_enc, urlConfig, rv_config, splash_config, native_config, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_Common extends ProtoAdapter<Common> {
        public ProtoAdapter_Common() {
            super(FieldEncoding.LENGTH_DELIMITED, Common.class);
        }

        @Override
        public int encodedSize(Common value) {
            return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.configRefresh) + ProtoAdapter.BOOL.encodedSizeWithTag(2, value.is_gdpr_region) + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.tracking_expiration_time) + ProtoAdapter.UINT32.encodedSizeWithTag(4, value.tracking_retry_interval) + ProtoAdapter.UINT32.encodedSizeWithTag(5, value.max_send_log_records) + ProtoAdapter.UINT32.encodedSizeWithTag(6, value.send_log_interval) + ProtoAdapter.UINT32.asRepeated().encodedSizeWithTag(7, value.dc_log_blacklist) + ProtoAdapter.BOOL.encodedSizeWithTag(8, value.enable_debug_level) + ProtoAdapter.UINT32.encodedSizeWithTag(9, value.load_interval) + ProtoAdapter.BOOL.encodedSizeWithTag(10, value.disable_up_location) + ProtoAdapter.BOOL.encodedSizeWithTag(11, value.log_enc) + UrlConfig.ADAPTER.encodedSizeWithTag(12, value.urlConfig) + RvConfig.ADAPTER.encodedSizeWithTag(13, value.rv_config) + SplashConfig.ADAPTER.encodedSizeWithTag(14, value.splash_config) + NativeConfig.ADAPTER.encodedSizeWithTag(15, value.native_config) + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, Common value) throws IOException {
            ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.configRefresh);
            ProtoAdapter.BOOL.encodeWithTag(writer, 2, value.is_gdpr_region);
            ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.tracking_expiration_time);
            ProtoAdapter.UINT32.encodeWithTag(writer, 4, value.tracking_retry_interval);
            ProtoAdapter.UINT32.encodeWithTag(writer, 5, value.max_send_log_records);
            ProtoAdapter.UINT32.encodeWithTag(writer, 6, value.send_log_interval);
            ProtoAdapter.UINT32.asRepeated().encodeWithTag(writer, 7, value.dc_log_blacklist);
            ProtoAdapter.BOOL.encodeWithTag(writer, 8, value.enable_debug_level);
            ProtoAdapter.UINT32.encodeWithTag(writer, 9, value.load_interval);
            ProtoAdapter.BOOL.encodeWithTag(writer, 10, value.disable_up_location);
            ProtoAdapter.BOOL.encodeWithTag(writer, 11, value.log_enc);
            UrlConfig.ADAPTER.encodeWithTag(writer, 12, value.urlConfig);
            RvConfig.ADAPTER.encodeWithTag(writer, 13, value.rv_config);
            SplashConfig.ADAPTER.encodeWithTag(writer, 14, value.splash_config);
            NativeConfig.ADAPTER.encodeWithTag(writer, 15, value.native_config);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public Common decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.configRefresh(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 2:
                        builder.is_gdpr_region(ProtoAdapter.BOOL.decode(reader));
                        break;
                    case 3:
                        builder.tracking_expiration_time(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 4:
                        builder.tracking_retry_interval(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 5:
                        builder.max_send_log_records(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 6:
                        builder.send_log_interval(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 7:
                        builder.dc_log_blacklist.add(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 8:
                        builder.enable_debug_level(ProtoAdapter.BOOL.decode(reader));
                        break;
                    case 9:
                        builder.load_interval(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 10:
                        builder.disable_up_location(ProtoAdapter.BOOL.decode(reader));
                        break;
                    case 11:
                        builder.log_enc(ProtoAdapter.BOOL.decode(reader));
                        break;
                    case 12:
                        builder.urlConfig(UrlConfig.ADAPTER.decode(reader));
                        break;
                    case 13:
                        builder.rv_config(RvConfig.ADAPTER.decode(reader));
                        break;
                    case 14:
                        builder.splash_config(SplashConfig.ADAPTER.decode(reader));
                        break;
                    case 15:
                        builder.native_config(NativeConfig.ADAPTER.decode(reader));
                        break;
                    default: {
                        FieldEncoding fieldEncoding = reader.peekFieldEncoding();
                        Object value = fieldEncoding.rawProtoAdapter().decode(reader);
                        builder.addUnknownField(tag, fieldEncoding, value);
                    }
                }
            }
            reader.endMessage(token);
            return builder.build();
        }

        @Override
        public Common redact(Common value) {
            Builder builder = value.newBuilder();
            if (builder.urlConfig != null)
                builder.urlConfig = UrlConfig.ADAPTER.redact(builder.urlConfig);
            if (builder.rv_config != null)
                builder.rv_config = RvConfig.ADAPTER.redact(builder.rv_config);
            if (builder.splash_config != null)
                builder.splash_config = SplashConfig.ADAPTER.redact(builder.splash_config);
            if (builder.native_config != null)
                builder.native_config = NativeConfig.ADAPTER.redact(builder.native_config);
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
