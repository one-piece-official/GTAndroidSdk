// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: sigmob_ssp_response.proto
package com.sigmob.sdk.base.models;

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

public final class WebEvent extends AndroidMessage<WebEvent, WebEvent.Builder> {
    public static final ProtoAdapter<WebEvent> ADAPTER = new ProtoAdapter_WebEvent();

    public static final Parcelable.Creator<WebEvent> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final String DEFAULT_EVENT_TYPE = "";

    public static final String DEFAULT_EVENT_NAME = "";

    /**
     * close,loaded,click
     */
    @WireField(
            tag = 1,
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final String event_type;

    /**
     * 各个dsp的html事件的具体名称
     */
    @WireField(
            tag = 2,
            adapter = "com.squareup.wire.ProtoAdapter#STRING"
    )
    public final String event_name;

    public WebEvent(String event_type, String event_name) {
        this(event_type, event_name, ByteString.EMPTY);
    }

    public WebEvent(String event_type, String event_name, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.event_type = event_type;
        this.event_name = event_name;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.event_type = event_type;
        builder.event_name = event_name;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof WebEvent)) return false;
        WebEvent o = (WebEvent) other;
        return unknownFields().equals(o.unknownFields())
                && Internal.equals(event_type, o.event_type)
                && Internal.equals(event_name, o.event_name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (event_type != null ? event_type.hashCode() : 0);
            result = result * 37 + (event_name != null ? event_name.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (event_type != null) builder.append(", event_type=").append(event_type);
        if (event_name != null) builder.append(", event_name=").append(event_name);
        return builder.replace(0, 2, "WebEvent{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<WebEvent, Builder> {
        public String event_type = DEFAULT_EVENT_TYPE;

        public String event_name = DEFAULT_EVENT_NAME;

        public Builder() {
        }

        /**
         * close,loaded,click
         */
        public Builder event_type(String event_type) {
            this.event_type = event_type;
            return this;
        }

        /**
         * 各个dsp的html事件的具体名称
         */
        public Builder event_name(String event_name) {
            this.event_name = event_name;
            return this;
        }

        @Override
        public WebEvent build() {
            return new WebEvent(event_type, event_name, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_WebEvent extends ProtoAdapter<WebEvent> {
        public ProtoAdapter_WebEvent() {
            super(FieldEncoding.LENGTH_DELIMITED, WebEvent.class);
        }

        @Override
        public int encodedSize(WebEvent value) {
            return ProtoAdapter.STRING.encodedSizeWithTag(1, value.event_type)
                    + ProtoAdapter.STRING.encodedSizeWithTag(2, value.event_name)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, WebEvent value) throws IOException {
            ProtoAdapter.STRING.encodeWithTag(writer, 1, value.event_type);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.event_name);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public WebEvent decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.event_type(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 2:
                        builder.event_name(ProtoAdapter.STRING.decode(reader));
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
        public WebEvent redact(WebEvent value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}