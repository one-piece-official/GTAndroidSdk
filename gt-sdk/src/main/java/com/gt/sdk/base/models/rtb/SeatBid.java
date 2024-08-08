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

/**
 * 广告信息
 */
public final class SeatBid extends AndroidMessage<SeatBid, SeatBid.Builder> {
    public static final ProtoAdapter<SeatBid> ADAPTER = new ProtoAdapter_SeatBid();

    public static final Parcelable.Creator<SeatBid> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final String DEFAULT_ADSLOT_ID = "";

    public static final String DEFAULT_VID = "";

    public static final String DEFAULT_CUST_ID = "";

    public static final String DEFAULT_CAMP_ID = "";

    public static final String DEFAULT_CRID = "";

    /**
     * 对应请求时填写的广告位ID
     */
    @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String adslot_id;

    /**
     * 广告曝光id.
     */
    @WireField(tag = 2, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String vid;

    /**
     * 客户ID
     */
    @WireField(tag = 3, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String cust_id;

    /**
     * 推广计划ID
     */
    @WireField(tag = 4, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String camp_id;

    /**
     * 创意ID
     */
    @WireField(tag = 5, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String crid;

    public SeatBid(String adslot_id, String vid, String cust_id, String camp_id, String crid) {
        this(adslot_id, vid, cust_id, camp_id, crid, ByteString.EMPTY);
    }

    public SeatBid(String adslot_id, String vid, String cust_id, String camp_id, String crid, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.adslot_id = adslot_id;
        this.vid = vid;
        this.cust_id = cust_id;
        this.camp_id = camp_id;
        this.crid = crid;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.adslot_id = adslot_id;
        builder.vid = vid;
        builder.cust_id = cust_id;
        builder.camp_id = camp_id;
        builder.crid = crid;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SeatBid)) return false;
        SeatBid o = (SeatBid) other;
        return unknownFields().equals(o.unknownFields()) && Internal.equals(adslot_id, o.adslot_id) && Internal.equals(vid, o.vid) && Internal.equals(cust_id, o.cust_id) && Internal.equals(camp_id, o.camp_id) && Internal.equals(crid, o.crid);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (adslot_id != null ? adslot_id.hashCode() : 0);
            result = result * 37 + (vid != null ? vid.hashCode() : 0);
            result = result * 37 + (cust_id != null ? cust_id.hashCode() : 0);
            result = result * 37 + (camp_id != null ? camp_id.hashCode() : 0);
            result = result * 37 + (crid != null ? crid.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (adslot_id != null) builder.append(", adslot_id=").append(adslot_id);
        if (vid != null) builder.append(", vid=").append(vid);
        if (cust_id != null) builder.append(", cust_id=").append(cust_id);
        if (camp_id != null) builder.append(", camp_id=").append(camp_id);
        if (crid != null) builder.append(", crid=").append(crid);
        return builder.replace(0, 2, "SeatBid{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<SeatBid, Builder> {
        public String adslot_id;

        public String vid;

        public String cust_id;

        public String camp_id;

        public String crid;

        public Builder() {
        }

        /**
         * 对应请求时填写的广告位ID
         */
        public Builder adslot_id(String adslot_id) {
            this.adslot_id = adslot_id;
            return this;
        }

        /**
         * 广告曝光id.
         */
        public Builder vid(String vid) {
            this.vid = vid;
            return this;
        }

        /**
         * 客户ID
         */
        public Builder cust_id(String cust_id) {
            this.cust_id = cust_id;
            return this;
        }

        /**
         * 推广计划ID
         */
        public Builder camp_id(String camp_id) {
            this.camp_id = camp_id;
            return this;
        }

        /**
         * 创意ID
         */
        public Builder crid(String crid) {
            this.crid = crid;
            return this;
        }

        @Override
        public SeatBid build() {
            return new SeatBid(adslot_id, vid, cust_id, camp_id, crid, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_SeatBid extends ProtoAdapter<SeatBid> {
        public ProtoAdapter_SeatBid() {
            super(FieldEncoding.LENGTH_DELIMITED, SeatBid.class);
        }

        @Override
        public int encodedSize(SeatBid value) {
            return ProtoAdapter.STRING.encodedSizeWithTag(1, value.adslot_id) + ProtoAdapter.STRING.encodedSizeWithTag(2, value.vid) + ProtoAdapter.STRING.encodedSizeWithTag(3, value.cust_id) + ProtoAdapter.STRING.encodedSizeWithTag(4, value.camp_id) + ProtoAdapter.STRING.encodedSizeWithTag(5, value.crid) + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, SeatBid value) throws IOException {
            ProtoAdapter.STRING.encodeWithTag(writer, 1, value.adslot_id);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.vid);
            ProtoAdapter.STRING.encodeWithTag(writer, 3, value.cust_id);
            ProtoAdapter.STRING.encodeWithTag(writer, 4, value.camp_id);
            ProtoAdapter.STRING.encodeWithTag(writer, 5, value.crid);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public SeatBid decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.adslot_id(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 2:
                        builder.vid(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 3:
                        builder.cust_id(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 4:
                        builder.camp_id(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 5:
                        builder.crid(ProtoAdapter.STRING.decode(reader));
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
        public SeatBid redact(SeatBid value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}