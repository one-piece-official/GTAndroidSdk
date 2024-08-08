// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: gt_ad_response.proto
package com.gt.sdk.base.models.rtb;

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
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;

public final class Bid extends AndroidMessage<Bid, Bid.Builder> {
    public static final ProtoAdapter<Bid> ADAPTER = new ProtoAdapter_Bid();

    public static final Parcelable.Creator<Bid> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final String DEFAULT_ID = "";

    public static final String DEFAULT_ADID = "";

    public static final String DEFAULT_IMPID = "";

    public static final Integer DEFAULT_PRICE = 0;

    public static final Integer DEFAULT_ACTION = 0;

    public static final String DEFAULT_TARGET_URL = "";

    public static final String DEFAULT_LANDING_URL = "";

    public static final String DEFAULT_AD_LOGO = "";

    public static final Integer DEFAULT_PROTOCOL_TYPE = 0;

    public static final String DEFAULT_APP_NAME = "";

    public static final String DEFAULT_BRAND_NAME = "";

    public static final String DEFAULT_PACKAGE_NAME = "";

    public static final Integer DEFAULT_APP_SIZE = 0;

    public static final String DEFAULT_APP_VERSION = "";

    public static final String DEFAULT_DEVELOPER = "";

    public static final String DEFAULT_PRIVACY_URL = "";

    public static final String DEFAULT_PERMISSION_URL = "";

    /**
     * 用于记录日志或行为追踪
     */
    @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String id;

    /**
     * 生成的素材校验 id
     */
    @WireField(tag = 2, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String adid;

    /**
     * 对应请求中的 imp 的 id
     */
    @WireField(tag = 3, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String impid;

    /**
     * 实际出价。如果是RTB，则必填，否则请忽略
     */
    @WireField(tag = 4, adapter = "com.squareup.wire.ProtoAdapter#INT32")
    public final Integer price;

    /**
     * 统计上报地址，查看 Tracking 对象
     */
    @WireField(tag = 5, adapter = "com.gt.sdk.base.models.rtb.Tracking#ADAPTER")
    public final Tracking tracking;

    /**
     * 广告动作类型：1 代表点击跳转，2 代表唤醒，3代表下载
     */
    @WireField(tag = 6, adapter = "com.squareup.wire.ProtoAdapter#INT32")
    public final Integer action;

    /**
     * deeplink 对应的吊起链接或 App 的下载链接
     */
    @WireField(tag = 7, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String target_url;

    /**
     * 落地页的 H5 链接
     */
    @WireField(tag = 9, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String landing_url;

    /**
     * 广告主logo
     */
    @WireField(tag = 8, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String ad_logo;

    /**
     * 广告素材，查看 Adm 对象
     */
    @WireField(tag = 10, adapter = "com.gt.sdk.base.models.rtb.Adm#ADAPTER")
    public final Adm adm;

    /**
     * 下载相关，可选值：0 正常下载类型，1 广点通
     */
    @WireField(tag = 11, adapter = "com.squareup.wire.ProtoAdapter#INT32")
    public final Integer protocol_type;

    /**
     * 下载相关，应用名
     */
    @WireField(tag = 12, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String app_name;

    /**
     * 下载相关，应用品牌名
     */
    @WireField(tag = 13, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String brand_name;

    /**
     * 下载相关，应用包名
     */
    @WireField(tag = 14, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String package_name;

    /**
     * 下载相关，应用大小
     */
    @WireField(tag = 15, adapter = "com.squareup.wire.ProtoAdapter#INT32")
    public final Integer app_size;

    /**
     * 下载相关，应用版本
     */
    @WireField(tag = 16, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String app_version;

    /**
     * 下载相关，应用开发者
     */
    @WireField(tag = 17, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String developer;

    /**
     * 下载相关，隐私协议
     */
    @WireField(tag = 18, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String privacy_url;

    /**
     * 下载相关，用户权限
     */
    @WireField(tag = 19, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String permission_url;

    public Bid(String id, String adid, String impid, Integer price, Tracking tracking, Integer action, String target_url, String landing_url, String ad_logo, Adm adm, Integer protocol_type, String app_name, String brand_name, String package_name, Integer app_size, String app_version, String developer, String privacy_url, String permission_url) {
        this(id, adid, impid, price, tracking, action, target_url, landing_url, ad_logo, adm, protocol_type, app_name, brand_name, package_name, app_size, app_version, developer, privacy_url, permission_url, ByteString.EMPTY);
    }

    public Bid(String id, String adid, String impid, Integer price, Tracking tracking, Integer action, String target_url, String landing_url, String ad_logo, Adm adm, Integer protocol_type, String app_name, String brand_name, String package_name, Integer app_size, String app_version, String developer, String privacy_url, String permission_url, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.id = id;
        this.adid = adid;
        this.impid = impid;
        this.price = price;
        this.tracking = tracking;
        this.action = action;
        this.target_url = target_url;
        this.landing_url = landing_url;
        this.ad_logo = ad_logo;
        this.adm = adm;
        this.protocol_type = protocol_type;
        this.app_name = app_name;
        this.brand_name = brand_name;
        this.package_name = package_name;
        this.app_size = app_size;
        this.app_version = app_version;
        this.developer = developer;
        this.privacy_url = privacy_url;
        this.permission_url = permission_url;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.id = id;
        builder.adid = adid;
        builder.impid = impid;
        builder.price = price;
        builder.tracking = tracking;
        builder.action = action;
        builder.target_url = target_url;
        builder.landing_url = landing_url;
        builder.ad_logo = ad_logo;
        builder.adm = adm;
        builder.protocol_type = protocol_type;
        builder.app_name = app_name;
        builder.brand_name = brand_name;
        builder.package_name = package_name;
        builder.app_size = app_size;
        builder.app_version = app_version;
        builder.developer = developer;
        builder.privacy_url = privacy_url;
        builder.permission_url = permission_url;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Bid)) return false;
        Bid o = (Bid) other;
        return unknownFields().equals(o.unknownFields()) && Internal.equals(id, o.id) && Internal.equals(adid, o.adid) && Internal.equals(impid, o.impid) && Internal.equals(price, o.price) && Internal.equals(tracking, o.tracking) && Internal.equals(action, o.action) && Internal.equals(target_url, o.target_url) && Internal.equals(landing_url, o.landing_url) && Internal.equals(ad_logo, o.ad_logo) && Internal.equals(adm, o.adm) && Internal.equals(protocol_type, o.protocol_type) && Internal.equals(app_name, o.app_name) && Internal.equals(brand_name, o.brand_name) && Internal.equals(package_name, o.package_name) && Internal.equals(app_size, o.app_size) && Internal.equals(app_version, o.app_version) && Internal.equals(developer, o.developer) && Internal.equals(privacy_url, o.privacy_url) && Internal.equals(permission_url, o.permission_url);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (id != null ? id.hashCode() : 0);
            result = result * 37 + (adid != null ? adid.hashCode() : 0);
            result = result * 37 + (impid != null ? impid.hashCode() : 0);
            result = result * 37 + (price != null ? price.hashCode() : 0);
            result = result * 37 + (tracking != null ? tracking.hashCode() : 0);
            result = result * 37 + (action != null ? action.hashCode() : 0);
            result = result * 37 + (target_url != null ? target_url.hashCode() : 0);
            result = result * 37 + (landing_url != null ? landing_url.hashCode() : 0);
            result = result * 37 + (ad_logo != null ? ad_logo.hashCode() : 0);
            result = result * 37 + (adm != null ? adm.hashCode() : 0);
            result = result * 37 + (protocol_type != null ? protocol_type.hashCode() : 0);
            result = result * 37 + (app_name != null ? app_name.hashCode() : 0);
            result = result * 37 + (brand_name != null ? brand_name.hashCode() : 0);
            result = result * 37 + (package_name != null ? package_name.hashCode() : 0);
            result = result * 37 + (app_size != null ? app_size.hashCode() : 0);
            result = result * 37 + (app_version != null ? app_version.hashCode() : 0);
            result = result * 37 + (developer != null ? developer.hashCode() : 0);
            result = result * 37 + (privacy_url != null ? privacy_url.hashCode() : 0);
            result = result * 37 + (permission_url != null ? permission_url.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (id != null) builder.append(", id=").append(id);
        if (adid != null) builder.append(", adid=").append(adid);
        if (impid != null) builder.append(", impid=").append(impid);
        if (price != null) builder.append(", price=").append(price);
        if (tracking != null) builder.append(", tracking=").append(tracking);
        if (action != null) builder.append(", action=").append(action);
        if (target_url != null) builder.append(", target_url=").append(target_url);
        if (landing_url != null) builder.append(", landing_url=").append(landing_url);
        if (ad_logo != null) builder.append(", ad_logo=").append(ad_logo);
        if (adm != null) builder.append(", adm=").append(adm);
        if (protocol_type != null) builder.append(", protocol_type=").append(protocol_type);
        if (app_name != null) builder.append(", app_name=").append(app_name);
        if (brand_name != null) builder.append(", brand_name=").append(brand_name);
        if (package_name != null) builder.append(", package_name=").append(package_name);
        if (app_size != null) builder.append(", app_size=").append(app_size);
        if (app_version != null) builder.append(", app_version=").append(app_version);
        if (developer != null) builder.append(", developer=").append(developer);
        if (privacy_url != null) builder.append(", privacy_url=").append(privacy_url);
        if (permission_url != null) builder.append(", permission_url=").append(permission_url);
        return builder.replace(0, 2, "Bid{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<Bid, Builder> {
        public String id;

        public String adid;

        public String impid;

        public Integer price;

        public Tracking tracking;

        public Integer action;

        public String target_url;

        public String landing_url;

        public String ad_logo;

        public Adm adm;

        public Integer protocol_type;

        public String app_name;

        public String brand_name;

        public String package_name;

        public Integer app_size;

        public String app_version;

        public String developer;

        public String privacy_url;

        public String permission_url;

        public Builder() {
        }

        /**
         * 用于记录日志或行为追踪
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * 生成的素材校验 id
         */
        public Builder adid(String adid) {
            this.adid = adid;
            return this;
        }

        /**
         * 对应请求中的 imp 的 id
         */
        public Builder impid(String impid) {
            this.impid = impid;
            return this;
        }

        /**
         * 实际出价。如果是RTB，则必填，否则请忽略
         */
        public Builder price(Integer price) {
            this.price = price;
            return this;
        }

        /**
         * 统计上报地址，查看 Tracking 对象
         */
        public Builder tracking(Tracking tracking) {
            this.tracking = tracking;
            return this;
        }

        /**
         * 广告动作类型：1 代表点击跳转，2 代表唤醒，3代表下载
         */
        public Builder action(Integer action) {
            this.action = action;
            return this;
        }

        /**
         * deeplink 对应的吊起链接或 App 的下载链接
         */
        public Builder target_url(String target_url) {
            this.target_url = target_url;
            return this;
        }

        /**
         * 落地页的 H5 链接
         */
        public Builder landing_url(String landing_url) {
            this.landing_url = landing_url;
            return this;
        }

        /**
         * 广告主logo
         */
        public Builder ad_logo(String ad_logo) {
            this.ad_logo = ad_logo;
            return this;
        }

        /**
         * 广告素材，查看 Adm 对象
         */
        public Builder adm(Adm adm) {
            this.adm = adm;
            return this;
        }

        /**
         * 下载相关，可选值：0 正常下载类型，1 广点通
         */
        public Builder protocol_type(Integer protocol_type) {
            this.protocol_type = protocol_type;
            return this;
        }

        /**
         * 下载相关，应用名
         */
        public Builder app_name(String app_name) {
            this.app_name = app_name;
            return this;
        }

        /**
         * 下载相关，应用品牌名
         */
        public Builder brand_name(String brand_name) {
            this.brand_name = brand_name;
            return this;
        }

        /**
         * 下载相关，应用包名
         */
        public Builder package_name(String package_name) {
            this.package_name = package_name;
            return this;
        }

        /**
         * 下载相关，应用大小
         */
        public Builder app_size(Integer app_size) {
            this.app_size = app_size;
            return this;
        }

        /**
         * 下载相关，应用版本
         */
        public Builder app_version(String app_version) {
            this.app_version = app_version;
            return this;
        }

        /**
         * 下载相关，应用开发者
         */
        public Builder developer(String developer) {
            this.developer = developer;
            return this;
        }

        /**
         * 下载相关，隐私协议
         */
        public Builder privacy_url(String privacy_url) {
            this.privacy_url = privacy_url;
            return this;
        }

        /**
         * 下载相关，用户权限
         */
        public Builder permission_url(String permission_url) {
            this.permission_url = permission_url;
            return this;
        }

        @Override
        public Bid build() {
            return new Bid(id, adid, impid, price, tracking, action, target_url, landing_url, ad_logo, adm, protocol_type, app_name, brand_name, package_name, app_size, app_version, developer, privacy_url, permission_url, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_Bid extends ProtoAdapter<Bid> {
        public ProtoAdapter_Bid() {
            super(FieldEncoding.LENGTH_DELIMITED, Bid.class);
        }

        @Override
        public int encodedSize(Bid value) {
            return ProtoAdapter.STRING.encodedSizeWithTag(1, value.id) + ProtoAdapter.STRING.encodedSizeWithTag(2, value.adid) + ProtoAdapter.STRING.encodedSizeWithTag(3, value.impid) + ProtoAdapter.INT32.encodedSizeWithTag(4, value.price) + Tracking.ADAPTER.encodedSizeWithTag(5, value.tracking) + ProtoAdapter.INT32.encodedSizeWithTag(6, value.action) + ProtoAdapter.STRING.encodedSizeWithTag(7, value.target_url) + ProtoAdapter.STRING.encodedSizeWithTag(9, value.landing_url) + ProtoAdapter.STRING.encodedSizeWithTag(8, value.ad_logo) + Adm.ADAPTER.encodedSizeWithTag(10, value.adm) + ProtoAdapter.INT32.encodedSizeWithTag(11, value.protocol_type) + ProtoAdapter.STRING.encodedSizeWithTag(12, value.app_name) + ProtoAdapter.STRING.encodedSizeWithTag(13, value.brand_name) + ProtoAdapter.STRING.encodedSizeWithTag(14, value.package_name) + ProtoAdapter.INT32.encodedSizeWithTag(15, value.app_size) + ProtoAdapter.STRING.encodedSizeWithTag(16, value.app_version) + ProtoAdapter.STRING.encodedSizeWithTag(17, value.developer) + ProtoAdapter.STRING.encodedSizeWithTag(18, value.privacy_url) + ProtoAdapter.STRING.encodedSizeWithTag(19, value.permission_url) + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, Bid value) throws IOException {
            ProtoAdapter.STRING.encodeWithTag(writer, 1, value.id);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.adid);
            ProtoAdapter.STRING.encodeWithTag(writer, 3, value.impid);
            ProtoAdapter.INT32.encodeWithTag(writer, 4, value.price);
            Tracking.ADAPTER.encodeWithTag(writer, 5, value.tracking);
            ProtoAdapter.INT32.encodeWithTag(writer, 6, value.action);
            ProtoAdapter.STRING.encodeWithTag(writer, 7, value.target_url);
            ProtoAdapter.STRING.encodeWithTag(writer, 9, value.landing_url);
            ProtoAdapter.STRING.encodeWithTag(writer, 8, value.ad_logo);
            Adm.ADAPTER.encodeWithTag(writer, 10, value.adm);
            ProtoAdapter.INT32.encodeWithTag(writer, 11, value.protocol_type);
            ProtoAdapter.STRING.encodeWithTag(writer, 12, value.app_name);
            ProtoAdapter.STRING.encodeWithTag(writer, 13, value.brand_name);
            ProtoAdapter.STRING.encodeWithTag(writer, 14, value.package_name);
            ProtoAdapter.INT32.encodeWithTag(writer, 15, value.app_size);
            ProtoAdapter.STRING.encodeWithTag(writer, 16, value.app_version);
            ProtoAdapter.STRING.encodeWithTag(writer, 17, value.developer);
            ProtoAdapter.STRING.encodeWithTag(writer, 18, value.privacy_url);
            ProtoAdapter.STRING.encodeWithTag(writer, 19, value.permission_url);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public Bid decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.id(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 2:
                        builder.adid(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 3:
                        builder.impid(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 4:
                        builder.price(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 5:
                        builder.tracking(Tracking.ADAPTER.decode(reader));
                        break;
                    case 6:
                        builder.action(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 7:
                        builder.target_url(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 8:
                        builder.ad_logo(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 9:
                        builder.landing_url(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 10:
                        builder.adm(Adm.ADAPTER.decode(reader));
                        break;
                    case 11:
                        builder.protocol_type(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 12:
                        builder.app_name(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 13:
                        builder.brand_name(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 14:
                        builder.package_name(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 15:
                        builder.app_size(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 16:
                        builder.app_version(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 17:
                        builder.developer(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 18:
                        builder.privacy_url(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 19:
                        builder.permission_url(ProtoAdapter.STRING.decode(reader));
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
        public Bid redact(Bid value) {
            Builder builder = value.newBuilder();
            if (builder.tracking != null)
                builder.tracking = Tracking.ADAPTER.redact(builder.tracking);
            if (builder.adm != null) builder.adm = Adm.ADAPTER.redact(builder.adm);
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}