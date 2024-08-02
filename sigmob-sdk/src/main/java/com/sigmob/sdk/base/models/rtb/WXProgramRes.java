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


public final class WXProgramRes extends AndroidMessage<WXProgramRes, WXProgramRes.Builder> {
    public static final ProtoAdapter<WXProgramRes> ADAPTER = new ProtoAdapter_WXProgramRes();

    public static final Parcelable.Creator<WXProgramRes> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final String DEFAULT_WX_APP_ID = "";

    public static final String DEFAULT_WX_UNIVERSAL_LINK = "";

    public static final String DEFAULT_WX_APP_USERNAME = "";

    public static final String DEFAULT_WX_APP_PATH = "";

    /**
     * 微信开放平台媒体id
     */
    @WireField(
            tag = 1,
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final String wx_app_id;

    /**
     * 微信开放平台媒体UniversalLink（仅iOS需要）
     */
    @WireField(
            tag = 2,
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final String wx_universal_link;

    /**
     * 小程序原始id
     */
    @WireField(
            tag = 3,
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final String wx_app_username;

    /**
     * 拉起小程序页面的可带参路径
     */
    @WireField(
            tag = 4,
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final String wx_app_path;

    public WXProgramRes(String wx_app_id, String wx_universal_link, String wx_app_username,
                        String wx_app_path) {
        this(wx_app_id, wx_universal_link, wx_app_username, wx_app_path, ByteString.EMPTY);
    }

    public WXProgramRes(String wx_app_id, String wx_universal_link, String wx_app_username,
                        String wx_app_path, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.wx_app_id = wx_app_id;
        this.wx_universal_link = wx_universal_link;
        this.wx_app_username = wx_app_username;
        this.wx_app_path = wx_app_path;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.wx_app_id = wx_app_id;
        builder.wx_universal_link = wx_universal_link;
        builder.wx_app_username = wx_app_username;
        builder.wx_app_path = wx_app_path;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof WXProgramRes)) return false;
        WXProgramRes o = (WXProgramRes) other;
        return unknownFields().equals(o.unknownFields())
                && Internal.equals(wx_app_id, o.wx_app_id)
                && Internal.equals(wx_universal_link, o.wx_universal_link)
                && Internal.equals(wx_app_username, o.wx_app_username)
                && Internal.equals(wx_app_path, o.wx_app_path);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (wx_app_id != null ? wx_app_id.hashCode() : 0);
            result = result * 37 + (wx_universal_link != null ? wx_universal_link.hashCode() : 0);
            result = result * 37 + (wx_app_username != null ? wx_app_username.hashCode() : 0);
            result = result * 37 + (wx_app_path != null ? wx_app_path.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (wx_app_id != null) builder.append(", wx_app_id=").append(wx_app_id);
        if (wx_universal_link != null)
            builder.append(", wx_universal_link=").append(wx_universal_link);
        if (wx_app_username != null) builder.append(", wx_app_username=").append(wx_app_username);
        if (wx_app_path != null) builder.append(", wx_app_path=").append(wx_app_path);
        return builder.replace(0, 2, "WXProgramRes{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<WXProgramRes, Builder> {
        public String wx_app_id = DEFAULT_WX_APP_ID;

        public String wx_universal_link = DEFAULT_WX_UNIVERSAL_LINK;

        public String wx_app_username = DEFAULT_WX_APP_USERNAME;

        public String wx_app_path = DEFAULT_WX_APP_PATH;

        public Builder() {
        }

        /**
         * 微信开放平台媒体id
         */
        public Builder wx_app_id(String wx_app_id) {
            this.wx_app_id = wx_app_id;
            return this;
        }

        /**
         * 微信开放平台媒体UniversalLink（仅iOS需要）
         */
        public Builder wx_universal_link(String wx_universal_link) {
            this.wx_universal_link = wx_universal_link;
            return this;
        }

        /**
         * 小程序原始id
         */
        public Builder wx_app_username(String wx_app_username) {
            this.wx_app_username = wx_app_username;
            return this;
        }

        /**
         * 拉起小程序页面的可带参路径
         */
        public Builder wx_app_path(String wx_app_path) {
            this.wx_app_path = wx_app_path;
            return this;
        }

        @Override
        public WXProgramRes build() {
            return new WXProgramRes(wx_app_id, wx_universal_link, wx_app_username, wx_app_path, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_WXProgramRes extends ProtoAdapter<WXProgramRes> {
        public ProtoAdapter_WXProgramRes() {
            super(FieldEncoding.LENGTH_DELIMITED, WXProgramRes.class);
        }

        @Override
        public int encodedSize(WXProgramRes value) {
            return ProtoAdapter.STRING.encodedSizeWithTag(1, value.wx_app_id)
                    + ProtoAdapter.STRING.encodedSizeWithTag(2, value.wx_universal_link)
                    + ProtoAdapter.STRING.encodedSizeWithTag(3, value.wx_app_username)
                    + ProtoAdapter.STRING.encodedSizeWithTag(4, value.wx_app_path)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, WXProgramRes value) throws IOException {
            ProtoAdapter.STRING.encodeWithTag(writer, 1, value.wx_app_id);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.wx_universal_link);
            ProtoAdapter.STRING.encodeWithTag(writer, 3, value.wx_app_username);
            ProtoAdapter.STRING.encodeWithTag(writer, 4, value.wx_app_path);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public WXProgramRes decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.wx_app_id(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 2:
                        builder.wx_universal_link(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 3:
                        builder.wx_app_username(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 4:
                        builder.wx_app_path(ProtoAdapter.STRING.decode(reader));
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
        public WXProgramRes redact(WXProgramRes value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
