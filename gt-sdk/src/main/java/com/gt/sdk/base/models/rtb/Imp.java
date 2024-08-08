// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: gt_ad_request.proto
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
import java.util.Map;

public final class Imp extends AndroidMessage<Imp, Imp.Builder> {

    public static final ProtoAdapter<Imp> ADAPTER = new ProtoAdapter_Imp();

    public static final Parcelable.Creator<Imp> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final String DEFAULT_ID = "";

    public static final String DEFAULT_TAGID = "";

    public static final String DEFAULT_SUBTAGID = "";

    public static final Integer DEFAULT_STYLE = 0;

    public static final Integer DEFAULT_SECURE = 0;

    public static final Integer DEFAULT_BIDFLOOR = 0;

    public static final Integer DEFAULT_WIDTH = 0;

    public static final Integer DEFAULT_HEIGHT = 0;

    public static final Integer DEFAULT_DEEPLINK = 0;

    public static final Integer DEFAULT_UL = 0;

    @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String id;

    @WireField(tag = 2, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String tagid;

    @WireField(tag = 3, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String subtagid;

    @WireField(tag = 4, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer style;

    @WireField(tag = 5, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer secure;

    @WireField(tag = 6, keyAdapter = "com.squareup.wire.ProtoAdapter#STRING", adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final Map<String, String> ext;

    @WireField(tag = 7, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer bidfloor;

    @WireField(tag = 8, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer width;

    @WireField(tag = 9, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer height;

    @WireField(tag = 10, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer deeplink;

    @WireField(tag = 11, adapter = "com.squareup.wire.ProtoAdapter#UINT32")
    public final Integer ul;

    public Imp(String id, String tagid, String subtagid, Integer style, Integer secure, Map<String, String> ext, Integer bidfloor, Integer width, Integer height, Integer deeplink, Integer ul) {
        this(id, tagid, subtagid, style, secure, ext, bidfloor, width, height, deeplink, ul, ByteString.EMPTY);
    }

    public Imp(String id, String tagid, String subtagid, Integer style, Integer secure, Map<String, String> ext, Integer bidfloor, Integer width, Integer height, Integer deeplink, Integer ul, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.id = id;
        this.tagid = tagid;
        this.subtagid = subtagid;
        this.style = style;
        this.secure = secure;
        this.ext = Internal.immutableCopyOf("ext", ext);
        this.bidfloor = bidfloor;
        this.width = width;
        this.height = height;
        this.deeplink = deeplink;
        this.ul = ul;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.id = id;
        builder.tagid = tagid;
        builder.subtagid = subtagid;
        builder.style = style;
        builder.secure = secure;
        builder.ext = Internal.copyOf("ext", ext);
        builder.bidfloor = bidfloor;
        builder.width = width;
        builder.height = height;
        builder.deeplink = deeplink;
        builder.ul = ul;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Imp)) return false;
        Imp o = (Imp) other;
        return unknownFields().equals(o.unknownFields()) && Internal.equals(id, o.id) && Internal.equals(tagid, o.tagid) && Internal.equals(subtagid, o.subtagid) && Internal.equals(style, o.style) && Internal.equals(secure, o.secure) && ext.equals(o.ext) && Internal.equals(bidfloor, o.bidfloor) && Internal.equals(width, o.width) && Internal.equals(height, o.height) && Internal.equals(deeplink, o.deeplink) && Internal.equals(ul, o.ul);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (id != null ? id.hashCode() : 0);
            result = result * 37 + (tagid != null ? tagid.hashCode() : 0);
            result = result * 37 + (subtagid != null ? subtagid.hashCode() : 0);
            result = result * 37 + (style != null ? style.hashCode() : 0);
            result = result * 37 + (secure != null ? secure.hashCode() : 0);
            result = result * 37 + ext.hashCode();
            result = result * 37 + (bidfloor != null ? bidfloor.hashCode() : 0);
            result = result * 37 + (width != null ? width.hashCode() : 0);
            result = result * 37 + (height != null ? height.hashCode() : 0);
            result = result * 37 + (deeplink != null ? deeplink.hashCode() : 0);
            result = result * 37 + (ul != null ? ul.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (id != null) builder.append(", id=").append(id);
        if (tagid != null) builder.append(", tagid=").append(tagid);
        if (subtagid != null) builder.append(", subtagid=").append(subtagid);
        if (style != null) builder.append(", style=").append(style);
        if (secure != null) builder.append(", secure=").append(secure);
        if (!ext.isEmpty()) builder.append(", ext=").append(ext);
        if (bidfloor != null) builder.append(", bidfloor=").append(bidfloor);
        if (width != null) builder.append(", width=").append(width);
        if (height != null) builder.append(", height=").append(height);
        if (deeplink != null) builder.append(", deeplink=").append(deeplink);
        if (ul != null) builder.append(", ul=").append(ul);
        return builder.replace(0, 2, "Imp{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<Imp, Builder> {
        public String id = DEFAULT_ID;

        public String tagid = DEFAULT_TAGID;

        public String subtagid = DEFAULT_SUBTAGID;

        public Integer style = DEFAULT_STYLE;

        public Integer secure = DEFAULT_SECURE;

        public Map<String, String> ext;

        public Integer bidfloor = DEFAULT_BIDFLOOR;

        public Integer width = DEFAULT_WIDTH;

        public Integer height = DEFAULT_HEIGHT;

        public Integer deeplink = DEFAULT_DEEPLINK;

        public Integer ul = DEFAULT_UL;

        public Builder() {
            ext = Internal.newMutableMap();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder tagid(String tagid) {
            this.tagid = tagid;
            return this;
        }

        public Builder subtagid(String subtagid) {
            this.subtagid = subtagid;
            return this;
        }

        public Builder style(Integer style) {
            this.style = style;
            return this;
        }

        public Builder secure(Integer secure) {
            this.secure = secure;
            return this;
        }

        public Builder ext(Map<String, String> ext) {
            Internal.checkElementsNotNull(ext);
            this.ext = ext;
            return this;
        }

        public Builder bidfloor(Integer bidfloor) {
            this.bidfloor = bidfloor;
            return this;
        }

        public Builder width(Integer width) {
            this.width = width;
            return this;
        }

        public Builder height(Integer height) {
            this.height = height;
            return this;
        }

        public Builder deeplink(Integer deeplink) {
            this.deeplink = deeplink;
            return this;
        }

        public Builder ul(Integer ul) {
            this.ul = ul;
            return this;
        }

        @Override
        public Imp build() {
            return new Imp(id, tagid, subtagid, style, secure, ext, bidfloor, width, height, deeplink, ul, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_Imp extends ProtoAdapter<Imp> {
        private final ProtoAdapter<Map<String, String>> ext = ProtoAdapter.newMapAdapter(ProtoAdapter.STRING, ProtoAdapter.STRING);

        public ProtoAdapter_Imp() {
            super(FieldEncoding.LENGTH_DELIMITED, Imp.class);
        }

        @Override
        public int encodedSize(Imp value) {
            return ProtoAdapter.STRING.encodedSizeWithTag(1, value.id) + ProtoAdapter.STRING.encodedSizeWithTag(2, value.tagid) + ProtoAdapter.STRING.encodedSizeWithTag(3, value.subtagid) + ProtoAdapter.UINT32.encodedSizeWithTag(4, value.style) + ProtoAdapter.UINT32.encodedSizeWithTag(5, value.secure) + ext.encodedSizeWithTag(6, value.ext) + ProtoAdapter.UINT32.encodedSizeWithTag(7, value.bidfloor) + ProtoAdapter.UINT32.encodedSizeWithTag(8, value.width) + ProtoAdapter.UINT32.encodedSizeWithTag(9, value.height) + ProtoAdapter.UINT32.encodedSizeWithTag(10, value.deeplink) + ProtoAdapter.UINT32.encodedSizeWithTag(11, value.ul) + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, Imp value) throws IOException {
            ProtoAdapter.STRING.encodeWithTag(writer, 1, value.id);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.tagid);
            ProtoAdapter.STRING.encodeWithTag(writer, 3, value.subtagid);
            ProtoAdapter.UINT32.encodeWithTag(writer, 4, value.style);
            ProtoAdapter.UINT32.encodeWithTag(writer, 5, value.secure);
            ext.encodeWithTag(writer, 6, value.ext);
            ProtoAdapter.UINT32.encodeWithTag(writer, 7, value.bidfloor);
            ProtoAdapter.UINT32.encodeWithTag(writer, 8, value.width);
            ProtoAdapter.UINT32.encodeWithTag(writer, 9, value.height);
            ProtoAdapter.UINT32.encodeWithTag(writer, 10, value.deeplink);
            ProtoAdapter.UINT32.encodeWithTag(writer, 11, value.ul);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public Imp decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.id(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 2:
                        builder.tagid(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 3:
                        builder.subtagid(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 4:
                        builder.style(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 5:
                        builder.secure(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 6:
                        builder.ext.putAll(ext.decode(reader));
                        break;
                    case 7:
                        builder.bidfloor(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 8:
                        builder.width(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 9:
                        builder.height(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 10:
                        builder.deeplink(ProtoAdapter.UINT32.decode(reader));
                        break;
                    case 11:
                        builder.ul(ProtoAdapter.UINT32.decode(reader));
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
        public Imp redact(Imp value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}