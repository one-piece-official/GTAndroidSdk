// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: gt_common.proto
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

public final class User extends AndroidMessage<User, User.Builder> {

    public static final ProtoAdapter<User> ADAPTER = new ProtoAdapter_User();

    public static final Parcelable.Creator<User> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final String DEFAULT_ID = "";

    public static final String DEFAULT_GENDER = "";

    public static final String DEFAULT_KEYWORDS = "";

    /**
     * 用户ID
     */
    @WireField(tag = 1, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String id;

    /**
     * 用户性别，F 女性 M 男性
     */
    @WireField(tag = 2, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String gender;

    /**
     * 用户关键字
     */
    @WireField(tag = 3, adapter = "com.squareup.wire.ProtoAdapter#STRING")
    public final String keywords;

    public User(String id, String gender, String keywords) {
        this(id, gender, keywords, ByteString.EMPTY);
    }

    public User(String id, String gender, String keywords, ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.id = id;
        this.gender = gender;
        this.keywords = keywords;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.id = id;
        builder.gender = gender;
        builder.keywords = keywords;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof User)) return false;
        User o = (User) other;
        return unknownFields().equals(o.unknownFields()) && Internal.equals(id, o.id) && Internal.equals(gender, o.gender) && Internal.equals(keywords, o.keywords);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (id != null ? id.hashCode() : 0);
            result = result * 37 + (gender != null ? gender.hashCode() : 0);
            result = result * 37 + (keywords != null ? keywords.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (id != null) builder.append(", id=").append(id);
        if (gender != null) builder.append(", gender=").append(gender);
        if (keywords != null) builder.append(", keywords=").append(keywords);
        return builder.replace(0, 2, "User{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<User, Builder> {
        public String id = DEFAULT_ID;

        public String gender = DEFAULT_GENDER;

        public String keywords = DEFAULT_KEYWORDS;

        public Builder() {
        }

        /**
         * 用户ID
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * 用户性别，F 女性 M 男性
         */
        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        /**
         * 用户关键字
         */
        public Builder keywords(String keywords) {
            this.keywords = keywords;
            return this;
        }

        @Override
        public User build() {
            return new User(id, gender, keywords, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_User extends ProtoAdapter<User> {
        public ProtoAdapter_User() {
            super(FieldEncoding.LENGTH_DELIMITED, User.class);
        }

        @Override
        public int encodedSize(User value) {
            return ProtoAdapter.STRING.encodedSizeWithTag(1, value.id) + ProtoAdapter.STRING.encodedSizeWithTag(2, value.gender) + ProtoAdapter.STRING.encodedSizeWithTag(3, value.keywords) + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, User value) throws IOException {
            ProtoAdapter.STRING.encodeWithTag(writer, 1, value.id);
            ProtoAdapter.STRING.encodeWithTag(writer, 2, value.gender);
            ProtoAdapter.STRING.encodeWithTag(writer, 3, value.keywords);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public User decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.id(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 2:
                        builder.gender(ProtoAdapter.STRING.decode(reader));
                        break;
                    case 3:
                        builder.keywords(ProtoAdapter.STRING.decode(reader));
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
        public User redact(User value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}