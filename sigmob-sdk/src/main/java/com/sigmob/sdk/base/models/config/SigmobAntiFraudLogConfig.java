// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: sigmob_ssp_config.proto
package com.sigmob.sdk.base.models.config;

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

/**
 * 传感器配置信息
 */
public final class SigmobAntiFraudLogConfig extends AndroidMessage<SigmobAntiFraudLogConfig, SigmobAntiFraudLogConfig.Builder> {
    public static final ProtoAdapter<SigmobAntiFraudLogConfig> ADAPTER = new ProtoAdapter_SigmobAntiFraudLogConfig();

    public static final Parcelable.Creator<SigmobAntiFraudLogConfig> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    /**
     * 传感器配置，如果不配置，则认为不采集传感器信息
     */
    @WireField(
            tag = 1,
            adapter = "com.sigmob.sdk.base.models.config.SigmobMotionConfig#ADAPTER"
    )
    public final SigmobMotionConfig motion_config;

    /**
     * 触发事件列表['load', 'start', 'click', 'close']
     */
    @WireField(
            tag = 2,
            adapter = "com.squareup.wire.ProtoAdapter#STRING",
            label = WireField.Label.REPEATED
    )
    public final List<String> events;

    public SigmobAntiFraudLogConfig(SigmobMotionConfig motion_config, List<String> events) {
        this(motion_config, events, ByteString.EMPTY);
    }

    public SigmobAntiFraudLogConfig(SigmobMotionConfig motion_config, List<String> events,
                                    ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.motion_config = motion_config;
        this.events = Internal.immutableCopyOf("events", events);
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.motion_config = motion_config;
        builder.events = Internal.copyOf("events", events);
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SigmobAntiFraudLogConfig)) return false;
        SigmobAntiFraudLogConfig o = (SigmobAntiFraudLogConfig) other;
        return unknownFields().equals(o.unknownFields())
                && Internal.equals(motion_config, o.motion_config)
                && events.equals(o.events);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (motion_config != null ? motion_config.hashCode() : 0);
            result = result * 37 + events.hashCode();
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (motion_config != null) builder.append(", motion_config=").append(motion_config);
        if (!events.isEmpty()) builder.append(", events=").append(events);
        return builder.replace(0, 2, "SigmobAntiFraudLogConfig{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<SigmobAntiFraudLogConfig, Builder> {
        public SigmobMotionConfig motion_config;

        public List<String> events;

        public Builder() {
            events = Internal.newMutableList();
        }

        /**
         * 传感器配置，如果不配置，则认为不采集传感器信息
         */
        public Builder motion_config(SigmobMotionConfig motion_config) {
            this.motion_config = motion_config;
            return this;
        }

        /**
         * 触发事件列表['load', 'start', 'click', 'close']
         */
        public Builder events(List<String> events) {
            Internal.checkElementsNotNull(events);
            this.events = events;
            return this;
        }

        @Override
        public SigmobAntiFraudLogConfig build() {
            return new SigmobAntiFraudLogConfig(motion_config, events, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_SigmobAntiFraudLogConfig extends ProtoAdapter<SigmobAntiFraudLogConfig> {
        public ProtoAdapter_SigmobAntiFraudLogConfig() {
            super(FieldEncoding.LENGTH_DELIMITED, SigmobAntiFraudLogConfig.class);
        }

        @Override
        public int encodedSize(SigmobAntiFraudLogConfig value) {
            return SigmobMotionConfig.ADAPTER.encodedSizeWithTag(1, value.motion_config)
                    + ProtoAdapter.STRING.asRepeated().encodedSizeWithTag(2, value.events)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, SigmobAntiFraudLogConfig value) throws IOException {
            SigmobMotionConfig.ADAPTER.encodeWithTag(writer, 1, value.motion_config);
            ProtoAdapter.STRING.asRepeated().encodeWithTag(writer, 2, value.events);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public SigmobAntiFraudLogConfig decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.motion_config(SigmobMotionConfig.ADAPTER.decode(reader));
                        break;
                    case 2:
                        builder.events.add(ProtoAdapter.STRING.decode(reader));
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
        public SigmobAntiFraudLogConfig redact(SigmobAntiFraudLogConfig value) {
            Builder builder = value.newBuilder();
            if (builder.motion_config != null)
                builder.motion_config = SigmobMotionConfig.ADAPTER.redact(builder.motion_config);
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}