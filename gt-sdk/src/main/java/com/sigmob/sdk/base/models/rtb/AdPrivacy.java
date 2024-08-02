// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: sigmob_ssp_response.proto
package com.sigmob.sdk.base.models.rtb;

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
import java.util.Map;

public final class AdPrivacy extends AndroidMessage<AdPrivacy, AdPrivacy.Builder> {
    public static final ProtoAdapter<AdPrivacy> ADAPTER = new ProtoAdapter_AdPrivacy();

    public static final Parcelable.Creator<AdPrivacy> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final String DEFAULT_PRIVACY_INFO_URL = "";

    public static final String DEFAULT_PRIVACY_TEMPLATE_URL = "";

    /**
     * 下载广告隐私四要素信息页面地址，直接使用无需宏替换；当此字段存在忽略privacy_template_url和privacy_template_info
     */
    @WireField(
            tag = 1,
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final String privacy_info_url;

    /**
     * 下载广告隐私四要素信息页面模版地址，使用privacy_template_info中键值对进行宏替换
     */
    @WireField(
            tag = 2,
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final String privacy_template_url;

    /**
     * 下载广告隐私四要素信息页面模版信息；K: 宏名，V: 值
     */
    @WireField(
            tag = 3,
            keyAdapter = "com.squareup.wire.ProtoAdapter#STRING",
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final Map<String, String> privacy_template_info;

    public AdPrivacy(String privacy_info_url, String privacy_template_url,
                     Map<String, String> privacy_template_info) {
        this(privacy_info_url, privacy_template_url, privacy_template_info, ByteString.EMPTY);
    }

    public AdPrivacy(String privacy_info_url, String privacy_template_url,
                     Map<String, String> privacy_template_info, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.privacy_info_url = privacy_info_url;
        this.privacy_template_url = privacy_template_url;
        this.privacy_template_info = Internal.immutableCopyOf("privacy_template_info", privacy_template_info);
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.privacy_info_url = privacy_info_url;
        builder.privacy_template_url = privacy_template_url;
        builder.privacy_template_info = Internal.copyOf("privacy_template_info", privacy_template_info);
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof AdPrivacy)) return false;
        AdPrivacy o = (AdPrivacy) other;
        return unknownFields().equals(o.unknownFields())
                && Internal.equals(privacy_info_url, o.privacy_info_url)
                && Internal.equals(privacy_template_url, o.privacy_template_url)
                && privacy_template_info.equals(o.privacy_template_info);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (privacy_info_url != null ? privacy_info_url.hashCode() : 0);
            result = result * 37 + (privacy_template_url != null ? privacy_template_url.hashCode() : 0);
            result = result * 37 + privacy_template_info.hashCode();
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (privacy_info_url != null)
            builder.append(", privacy_info_url=").append(privacy_info_url);
        if (privacy_template_url != null)
            builder.append(", privacy_template_url=").append(privacy_template_url);
        if (!privacy_template_info.isEmpty())
            builder.append(", privacy_template_info=").append(privacy_template_info);
        return builder.replace(0, 2, "AdPrivacy{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<AdPrivacy, Builder> {
        public String privacy_info_url = DEFAULT_PRIVACY_INFO_URL;

        public String privacy_template_url = DEFAULT_PRIVACY_TEMPLATE_URL;

        public Map<String, String> privacy_template_info;

        public Builder() {
            privacy_template_info = Internal.newMutableMap();
        }

        /**
         * 下载广告隐私四要素信息页面地址，直接使用无需宏替换；当此字段存在忽略privacy_template_url和privacy_template_info
         */
        public Builder privacy_info_url(String privacy_info_url) {
            this.privacy_info_url = privacy_info_url;
            return this;
        }

        /**
         * 下载广告隐私四要素信息页面模版地址，使用privacy_template_info中键值对进行宏替换
         */
        public Builder privacy_template_url(String privacy_template_url) {
            this.privacy_template_url = privacy_template_url;
            return this;
        }

        /**
         * 下载广告隐私四要素信息页面模版信息；K: 宏名，V: 值
         */
        public Builder privacy_template_info(Map<String, String> privacy_template_info) {
            Internal.checkElementsNotNull(privacy_template_info);
            this.privacy_template_info = privacy_template_info;
            return this;
        }

        @Override
        public AdPrivacy build() {
            return new AdPrivacy(privacy_info_url, privacy_template_url, privacy_template_info, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_AdPrivacy extends ProtoAdapter<AdPrivacy> {
        private final ProtoAdapter<Map<String, String>> privacy_template_info = ProtoAdapter.newMapAdapter(ProtoAdapter.STRING, ProtoAdapter.STRING);

        public ProtoAdapter_AdPrivacy() {
            super(FieldEncoding.LENGTH_DELIMITED, AdPrivacy.class);
        }

        @Override
        public int encodedSize(AdPrivacy value) {
            return ProtoAdapter.STRING.encodedSizeWithTag(1, value.privacy_info_url)
                    + ProtoAdapter.STRING.encodedSizeWithTag(2, value.privacy_template_url)
                    + privacy_template_info.encodedSizeWithTag(3, value.privacy_template_info)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, AdPrivacy value) throws IOException {
            ProtoAdapter.STRING.encodeWithTag(writer, 1, value.privacy_info_url);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.privacy_template_url);
            privacy_template_info.encodeWithTag(writer, 3, value.privacy_template_info);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public AdPrivacy decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.privacy_info_url(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 2:
                        builder.privacy_template_url(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 3:
                        builder.privacy_template_info.putAll(privacy_template_info.decode(reader));
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
        public AdPrivacy redact(AdPrivacy value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}