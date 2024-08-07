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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;


public final class Adm extends AndroidMessage<Adm, Adm.Builder> {
    public static final ProtoAdapter<Adm> ADAPTER = new ProtoAdapter_Adm();

    public static final Parcelable.Creator<Adm> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final String DEFAULT_ID = "";

    public static final String DEFAULT_TITLE = "";

    public static final String DEFAULT_DESC = "";

    public static final String DEFAULT_BUTTON = "";

    public static final String DEFAULT_ICON = "";

    /**
     * 序号
     */
    @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String id;

    /**
     * 图片素材，查看 Image 对象
     */
    @WireField(tag = 2, adapter = "com.gt.sdk.base.models.rtb.Image#ADAPTER", label = WireField.Label.REPEATED)
    public final List<Image> img;

    /**
     * 图片素材，查看 Image 对象
     */
    @WireField(tag = 3, adapter = "com.gt.sdk.base.models.rtb.Video#ADAPTER")
    public final Video video;

    /**
     * 返回标题
     */
    @WireField(tag = 4, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String title;

    /**
     * 返回描述
     */
    @WireField(tag = 5, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String desc;

    /**
     * 按钮文案
     */
    @WireField(tag = 6, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String button;

    /**
     * 图标地址
     */
    @WireField(tag = 7, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String icon;

    public Adm(String id, List<Image> img, Video video, String title, String desc, String button, String icon) {
        this(id, img, video, title, desc, button, icon, ByteString.EMPTY);
    }

    public Adm(String id, List<Image> img, Video video, String title, String desc, String button, String icon, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.id = id;
        this.img = Internal.immutableCopyOf("img", img);
        this.video = video;
        this.title = title;
        this.desc = desc;
        this.button = button;
        this.icon = icon;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.id = id;
        builder.img = Internal.copyOf("img", img);
        builder.video = video;
        builder.title = title;
        builder.desc = desc;
        builder.button = button;
        builder.icon = icon;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Adm)) return false;
        Adm o = (Adm) other;
        return unknownFields().equals(o.unknownFields()) && Internal.equals(id, o.id) && img.equals(o.img) && Internal.equals(video, o.video) && Internal.equals(title, o.title) && Internal.equals(desc, o.desc) && Internal.equals(button, o.button) && Internal.equals(icon, o.icon);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (id != null ? id.hashCode() : 0);
            result = result * 37 + img.hashCode();
            result = result * 37 + (video != null ? video.hashCode() : 0);
            result = result * 37 + (title != null ? title.hashCode() : 0);
            result = result * 37 + (desc != null ? desc.hashCode() : 0);
            result = result * 37 + (button != null ? button.hashCode() : 0);
            result = result * 37 + (icon != null ? icon.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (id != null) builder.append(", id=").append(id);
        if (!img.isEmpty()) builder.append(", img=").append(img);
        if (video != null) builder.append(", video=").append(video);
        if (title != null) builder.append(", title=").append(title);
        if (desc != null) builder.append(", desc=").append(desc);
        if (button != null) builder.append(", button=").append(button);
        if (icon != null) builder.append(", icon=").append(icon);
        return builder.replace(0, 2, "Adm{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<Adm, Builder> {
        public String id;

        public List<Image> img;

        public Video video;

        public String title;

        public String desc;

        public String button;

        public String icon;

        public Builder() {
            img = Internal.newMutableList();
        }

        /**
         * 序号
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * 图片素材，查看 Image 对象
         */
        public Builder img(List<Image> img) {
            Internal.checkElementsNotNull(img);
            this.img = img;
            return this;
        }

        /**
         * 图片素材，查看 Image 对象
         */
        public Builder video(Video video) {
            this.video = video;
            return this;
        }

        /**
         * 返回标题
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * 返回描述
         */
        public Builder desc(String desc) {
            this.desc = desc;
            return this;
        }

        /**
         * 按钮文案
         */
        public Builder button(String button) {
            this.button = button;
            return this;
        }

        /**
         * 图标地址
         */
        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        @Override
        public Adm build() {
            return new Adm(id, img, video, title, desc, button, icon, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_Adm extends ProtoAdapter<Adm> {
        public ProtoAdapter_Adm() {
            super(FieldEncoding.LENGTH_DELIMITED, Adm.class);
        }

        @Override
        public int encodedSize(Adm value) {
            return ProtoAdapter.STRING.encodedSizeWithTag(1, value.id) + Image.ADAPTER.asRepeated().encodedSizeWithTag(2, value.img) + Video.ADAPTER.encodedSizeWithTag(3, value.video) + ProtoAdapter.STRING.encodedSizeWithTag(4, value.title) + ProtoAdapter.STRING.encodedSizeWithTag(5, value.desc) + ProtoAdapter.STRING.encodedSizeWithTag(6, value.button) + ProtoAdapter.STRING.encodedSizeWithTag(7, value.icon) + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, Adm value) throws IOException {
            ProtoAdapter.STRING.encodeWithTag(writer, 1, value.id);
            Image.ADAPTER.asRepeated().encodeWithTag(writer, 2, value.img);
            Video.ADAPTER.encodeWithTag(writer, 3, value.video);
            ProtoAdapter.STRING.encodeWithTag(writer, 4, value.title);
            ProtoAdapter.STRING.encodeWithTag(writer, 5, value.desc);
            ProtoAdapter.STRING.encodeWithTag(writer, 6, value.button);
            ProtoAdapter.STRING.encodeWithTag(writer, 7, value.icon);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public Adm decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.id(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 2:
                        builder.img.add(Image.ADAPTER.decode(reader));
                        break;
                    case 3:
                        builder.video(Video.ADAPTER.decode(reader));
                        break;
                    case 4:
                        builder.title(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 5:
                        builder.desc(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 6:
                        builder.button(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 7:
                        builder.icon(ProtoAdapter.STRING.decode(reader));
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
        public Adm redact(Adm value) {
            Builder builder = value.newBuilder();
            Internal.redactElements(builder.img, Image.ADAPTER);
            if (builder.video != null) builder.video = Video.ADAPTER.redact(builder.video);
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}
