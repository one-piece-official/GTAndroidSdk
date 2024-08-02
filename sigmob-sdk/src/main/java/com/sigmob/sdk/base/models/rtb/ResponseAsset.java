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

public final class ResponseAsset extends AndroidMessage<ResponseAsset, ResponseAsset.Builder> {
    public static final ProtoAdapter<ResponseAsset> ADAPTER = new ProtoAdapter_ResponseAsset();

    public static final Parcelable.Creator<ResponseAsset> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final Integer DEFAULT_INDEX = 0;

    /**
     * 原生元素id，通常从0开始递增
     */
    @WireField(
            tag = 1,
            adapter = "com.squareup.wire.ProtoAdapter#UINT32"
    )
    public final Integer index;

    /**
     * 视频元素
     */
    @WireField(
            tag = 2,
            adapter = "ResponseAssetVideo#ADAPTER"
    )
    public final ResponseAssetVideo video;

    /**
     * 图片元素
     */
    @WireField(
            tag = 3,
            adapter = "ResponseAssetImage#ADAPTER"
    )
    public final ResponseAssetImage image;

    /**
     * 文字元素
     */
    @WireField(
            tag = 4,
            adapter = "ResponseAssetText#ADAPTER"
    )
    public final ResponseAssetText text;

    public ResponseAsset(Integer index, ResponseAssetVideo video, ResponseAssetImage image,
                         ResponseAssetText text) {
        this(index, video, image, text, ByteString.EMPTY);
    }

    public ResponseAsset(Integer index, ResponseAssetVideo video, ResponseAssetImage image,
                         ResponseAssetText text, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.index = index;
        this.video = video;
        this.image = image;
        this.text = text;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.index = index;
        builder.video = video;
        builder.image = image;
        builder.text = text;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof ResponseAsset)) return false;
        ResponseAsset o = (ResponseAsset) other;
        return unknownFields().equals(o.unknownFields())
                && Internal.equals(index, o.index)
                && Internal.equals(video, o.video)
                && Internal.equals(image, o.image)
                && Internal.equals(text, o.text);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (index != null ? index.hashCode() : 0);
            result = result * 37 + (video != null ? video.hashCode() : 0);
            result = result * 37 + (image != null ? image.hashCode() : 0);
            result = result * 37 + (text != null ? text.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (index != null) builder.append(", index=").append(index);
        if (video != null) builder.append(", video=").append(video);
        if (image != null) builder.append(", image=").append(image);
        if (text != null) builder.append(", text=").append(text);
        return builder.replace(0, 2, "ResponseAsset{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<ResponseAsset, Builder> {
        public Integer index = DEFAULT_INDEX;

        public ResponseAssetVideo video;

        public ResponseAssetImage image;

        public ResponseAssetText text;

        public Builder() {
        }

        /**
         * 原生元素id，通常从0开始递增
         */
        public Builder index(Integer index) {
            this.index = index;
            return this;
        }

        /**
         * 视频元素
         */
        public Builder video(ResponseAssetVideo video) {
            this.video = video;
            return this;
        }

        /**
         * 图片元素
         */
        public Builder image(ResponseAssetImage image) {
            this.image = image;
            return this;
        }

        /**
         * 文字元素
         */
        public Builder text(ResponseAssetText text) {
            this.text = text;
            return this;
        }

        @Override
        public ResponseAsset build() {
            return new ResponseAsset(index, video, image, text, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_ResponseAsset extends ProtoAdapter<ResponseAsset> {
        public ProtoAdapter_ResponseAsset() {
            super(FieldEncoding.LENGTH_DELIMITED, ResponseAsset.class);
        }

        @Override
        public int encodedSize(ResponseAsset value) {
            return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.index)
                    + ResponseAssetVideo.ADAPTER.encodedSizeWithTag(2, value.video)
                    + ResponseAssetImage.ADAPTER.encodedSizeWithTag(3, value.image)
                    + ResponseAssetText.ADAPTER.encodedSizeWithTag(4, value.text)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, ResponseAsset value) throws IOException {
            ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.index);
            ResponseAssetVideo.ADAPTER.encodeWithTag(writer, 2, value.video);
            ResponseAssetImage.ADAPTER.encodeWithTag(writer, 3, value.image);
            ResponseAssetText.ADAPTER.encodeWithTag(writer, 4, value.text);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public ResponseAsset decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.index(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 2:
                        builder.video(ResponseAssetVideo.ADAPTER.decode(reader));
                        break;
                    case 3:
                        builder.image(ResponseAssetImage.ADAPTER.decode(reader));
                        break;
                    case 4:
                        builder.text(ResponseAssetText.ADAPTER.decode(reader));
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
        public ResponseAsset redact(ResponseAsset value) {
            Builder builder = value.newBuilder();
            if (builder.video != null)
                builder.video = ResponseAssetVideo.ADAPTER.redact(builder.video);
            if (builder.image != null)
                builder.image = ResponseAssetImage.ADAPTER.redact(builder.image);
            if (builder.text != null) builder.text = ResponseAssetText.ADAPTER.redact(builder.text);
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
