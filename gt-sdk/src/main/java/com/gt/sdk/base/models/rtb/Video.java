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


public final class Video extends AndroidMessage<Video, Video.Builder> {
    public static final ProtoAdapter<Video> ADAPTER = new ProtoAdapter_Video();

    public static final Parcelable.Creator<Video> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final Integer DEFAULT_W = 0;

    public static final Integer DEFAULT_H = 0;

    public static final String DEFAULT_URL = "";

    public static final Integer DEFAULT_DURATION = 0;

    public static final String DEFAULT_COVER = "";

    /**
     * 视频宽度
     */
    @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#INT32")
    public final Integer w;

    /**
     * 视频高度
     */
    @WireField(tag = 2, adapter = "com.squareup.wire.ProtoAdapter#INT32")
    public final Integer h;

    /**
     * 视频 URL
     */
    @WireField(tag = 3, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String url;

    /**
     * 视频时长
     */
    @WireField(tag = 4, adapter = "com.squareup.wire.ProtoAdapter#INT32")
    public final Integer duration;

    /**
     * 视频封面图
     */
    @WireField(tag = 5, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String cover;

    public Video(Integer w, Integer h, String url, Integer duration, String cover) {
        this(w, h, url, duration, cover, ByteString.EMPTY);
    }

    public Video(Integer w, Integer h, String url, Integer duration, String cover, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.w = w;
        this.h = h;
        this.url = url;
        this.duration = duration;
        this.cover = cover;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.w = w;
        builder.h = h;
        builder.url = url;
        builder.duration = duration;
        builder.cover = cover;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Video)) return false;
        Video o = (Video) other;
        return unknownFields().equals(o.unknownFields()) && Internal.equals(w, o.w) && Internal.equals(h, o.h) && Internal.equals(url, o.url) && Internal.equals(duration, o.duration) && Internal.equals(cover, o.cover);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (w != null ? w.hashCode() : 0);
            result = result * 37 + (h != null ? h.hashCode() : 0);
            result = result * 37 + (url != null ? url.hashCode() : 0);
            result = result * 37 + (duration != null ? duration.hashCode() : 0);
            result = result * 37 + (cover != null ? cover.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (w != null) builder.append(", w=").append(w);
        if (h != null) builder.append(", h=").append(h);
        if (url != null) builder.append(", url=").append(url);
        if (duration != null) builder.append(", duration=").append(duration);
        if (cover != null) builder.append(", cover=").append(cover);
        return builder.replace(0, 2, "Video{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<Video, Builder> {
        public Integer w;

        public Integer h;

        public String url;

        public Integer duration;

        public String cover;

        public Builder() {
        }

        /**
         * 视频宽度
         */
        public Builder w(Integer w) {
            this.w = w;
            return this;
        }

        /**
         * 视频高度
         */
        public Builder h(Integer h) {
            this.h = h;
            return this;
        }

        /**
         * 视频 URL
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * 视频时长
         */
        public Builder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        /**
         * 视频封面图
         */
        public Builder cover(String cover) {
            this.cover = cover;
            return this;
        }

        @Override
        public Video build() {
            return new Video(w, h, url, duration, cover, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_Video extends ProtoAdapter<Video> {
        public ProtoAdapter_Video() {
            super(FieldEncoding.LENGTH_DELIMITED, Video.class);
        }

        @Override
        public int encodedSize(Video value) {
            return ProtoAdapter.INT32.encodedSizeWithTag(1, value.w) + ProtoAdapter.INT32.encodedSizeWithTag(2, value.h) + ProtoAdapter.STRING.encodedSizeWithTag(3, value.url) + ProtoAdapter.INT32.encodedSizeWithTag(4, value.duration) + ProtoAdapter.STRING.encodedSizeWithTag(5, value.cover) + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, Video value) throws IOException {
            ProtoAdapter.INT32.encodeWithTag(writer, 1, value.w);
            ProtoAdapter.INT32.encodeWithTag(writer, 2, value.h);
            ProtoAdapter.STRING.encodeWithTag(writer, 3, value.url);
            ProtoAdapter.INT32.encodeWithTag(writer, 4, value.duration);
            ProtoAdapter.STRING.encodeWithTag(writer, 5, value.cover);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public Video decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.w(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 2:
                        builder.h(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 3:
                        builder.url(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 4:
                        builder.duration(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 5:
                        builder.cover(ProtoAdapter.STRING.decode(reader));
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
        public Video redact(Video value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}