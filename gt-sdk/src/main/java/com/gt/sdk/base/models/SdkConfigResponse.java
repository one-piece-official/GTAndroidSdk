// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: gt_ssp_config.proto
package com.gt.sdk.base.models;

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

import android.os.Parcelable;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;

public final class SdkConfigResponse extends AndroidMessage<SdkConfigResponse, SdkConfigResponse.Builder> {

    public static final ProtoAdapter<SdkConfigResponse> ADAPTER = new ProtoAdapter_SdkConfigResponse();

    public static final Parcelable.Creator<SdkConfigResponse> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final Integer DEFAULT_CODE = 0;

    public static final String DEFAULT_ERROR_MESSAGE = "";

    @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer code;

    @WireField(tag = 2, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String error_message;

    @WireField(tag = 3, adapter = "com.gt.sdk.base.models.SdkConfig#ADAPTER")
    public final SdkConfig config;

    public SdkConfigResponse(Integer code, String error_message, SdkConfig config) {
        this(code, error_message, config, ByteString.EMPTY);
    }

    public SdkConfigResponse(Integer code, String error_message, SdkConfig config, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.code = code;
        this.error_message = error_message;
        this.config = config;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.code = code;
        builder.error_message = error_message;
        builder.config = config;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SdkConfigResponse)) return false;
        SdkConfigResponse o = (SdkConfigResponse) other;
        return unknownFields().equals(o.unknownFields()) && Internal.equals(code, o.code) && Internal.equals(error_message, o.error_message) && Internal.equals(config, o.config);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (code != null ? code.hashCode() : 0);
            result = result * 37 + (error_message != null ? error_message.hashCode() : 0);
            result = result * 37 + (config != null ? config.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (code != null) builder.append(", code=").append(code);
        if (error_message != null) builder.append(", error_message=").append(error_message);
        if (config != null) builder.append(", config=").append(config);
        return builder.replace(0, 2, "SdkConfigResponse{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<SdkConfigResponse, Builder> {
        public Integer code = DEFAULT_CODE;

        public String error_message = DEFAULT_ERROR_MESSAGE;

        public SdkConfig config;

        public Builder() {
        }

        public Builder code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder error_message(String error_message) {
            this.error_message = error_message;
            return this;
        }

        public Builder config(SdkConfig config) {
            this.config = config;
            return this;
        }

        @Override
        public SdkConfigResponse build() {
            return new SdkConfigResponse(code, error_message, config, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_SdkConfigResponse extends ProtoAdapter<SdkConfigResponse> {
        public ProtoAdapter_SdkConfigResponse() {
            super(FieldEncoding.LENGTH_DELIMITED, SdkConfigResponse.class);
        }

        @Override
        public int encodedSize(SdkConfigResponse value) {
            return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.code) + ProtoAdapter.STRING.encodedSizeWithTag(2, value.error_message) + SdkConfig.ADAPTER.encodedSizeWithTag(3, value.config) + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, SdkConfigResponse value) throws IOException {
            ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.code);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.error_message);
            SdkConfig.ADAPTER.encodeWithTag(writer, 3, value.config);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public SdkConfigResponse decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.code(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 2:
                        builder.error_message(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 3:
                        builder.config(SdkConfig.ADAPTER.decode(reader));
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
        public SdkConfigResponse redact(SdkConfigResponse value) {
            Builder builder = value.newBuilder();
            if (builder.config != null) builder.config = SdkConfig.ADAPTER.redact(builder.config);
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}