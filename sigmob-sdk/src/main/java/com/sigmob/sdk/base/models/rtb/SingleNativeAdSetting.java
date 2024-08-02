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


/**
 * 单个原生广告配置
 */
public final class SingleNativeAdSetting extends AndroidMessage<SingleNativeAdSetting, SingleNativeAdSetting.Builder> {
    public static final ProtoAdapter<SingleNativeAdSetting> ADAPTER = new ProtoAdapter_SingleNativeAdSetting();

    public static final Parcelable.Creator<SingleNativeAdSetting> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final Boolean DEFAULT_USE_NA_VIDEO_COMPONENT = false;

    /**
     * 详情页是否使用NA视频组件
     */
    @WireField(
            tag = 1,
            adapter = "com.squareup.wire.ProtoAdapter#BOOL"
    )
    public final Boolean use_na_video_component;

    public SingleNativeAdSetting(Boolean use_na_video_component) {
        this(use_na_video_component, ByteString.EMPTY);
    }

    public SingleNativeAdSetting(Boolean use_na_video_component, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.use_na_video_component = use_na_video_component;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.use_na_video_component = use_na_video_component;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SingleNativeAdSetting)) return false;
        SingleNativeAdSetting o = (SingleNativeAdSetting) other;
        return unknownFields().equals(o.unknownFields())
                && Internal.equals(use_na_video_component, o.use_na_video_component);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (use_na_video_component != null ? use_na_video_component.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (use_na_video_component != null)
            builder.append(", use_na_video_component=").append(use_na_video_component);
        return builder.replace(0, 2, "SingleNativeAdSetting{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<SingleNativeAdSetting, Builder> {
        public Boolean use_na_video_component = DEFAULT_USE_NA_VIDEO_COMPONENT;

        public Builder() {
        }

        /**
         * 详情页是否使用NA视频组件
         */
        public Builder use_na_video_component(Boolean use_na_video_component) {
            this.use_na_video_component = use_na_video_component;
            return this;
        }

        @Override
        public SingleNativeAdSetting build() {
            return new SingleNativeAdSetting(use_na_video_component, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_SingleNativeAdSetting extends ProtoAdapter<SingleNativeAdSetting> {
        public ProtoAdapter_SingleNativeAdSetting() {
            super(FieldEncoding.LENGTH_DELIMITED, SingleNativeAdSetting.class);
        }

        @Override
        public int encodedSize(SingleNativeAdSetting value) {
            return ProtoAdapter.BOOL.encodedSizeWithTag(1, value.use_na_video_component)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, SingleNativeAdSetting value) throws IOException {
            ProtoAdapter.BOOL.encodeWithTag(writer, 1, value.use_na_video_component);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public SingleNativeAdSetting decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.use_na_video_component(ProtoAdapter.BOOL.decode(reader));
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
        public SingleNativeAdSetting redact(SingleNativeAdSetting value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
