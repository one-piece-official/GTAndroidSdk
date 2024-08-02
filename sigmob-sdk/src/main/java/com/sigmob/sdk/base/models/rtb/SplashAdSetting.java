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

public final class SplashAdSetting extends AndroidMessage<SplashAdSetting, SplashAdSetting.Builder> {
    public static final ProtoAdapter<SplashAdSetting> ADAPTER = new ProtoAdapter_SplashAdSetting();

    public static final Parcelable.Creator<SplashAdSetting> CREATOR = AndroidMessage.newCreator(ADAPTER);

    private static final long serialVersionUID = 0L;

    public static final Integer DEFAULT_SHOW_DURATION = 0;

    public static final Boolean DEFAULT_ENABLE_CLOSE_ON_CLICK = false;

    public static final Boolean DEFAULT_ENABLE_FULL_CLICK = false;

    public static final Boolean DEFAULT_INVISIBLE_AD_LABEL = false;

    public static final Boolean DEFAULT_USE_FLOATING_BTN = false;

    /**
     * 开屏倒计时时间,负值表示为定义，由sdk默认值决定（目前默认值是3）
     */
    @WireField(
            tag = 1,
            adapter = "com.squareup.wire.ProtoAdapter#INT32"
    )
    public final Integer show_duration;

    /**
     * 开屏点击关闭开关，（false 关闭，true 不关闭，默认值 false)
     */
    @WireField(
            tag = 2,
            adapter = "com.squareup.wire.ProtoAdapter#BOOL"
    )
    public final Boolean enable_close_on_click;

    /**
     * 是否开启全屏点击，default=false
     */
    @WireField(
            tag = 5,
            adapter = "com.squareup.wire.ProtoAdapter#BOOL"
    )
    public final Boolean enable_full_click;

    /**
     * 是否不显示广告字样
     */
    @WireField(
            tag = 6,
            adapter = "com.squareup.wire.ProtoAdapter#BOOL"
    )
    public final Boolean invisible_ad_label;

    /**
     * 模版是否使用浮窗按钮（除摇一摇模版外）
     */
    @WireField(
            tag = 7,
            adapter = "com.squareup.wire.ProtoAdapter#BOOL"
    )
    public final Boolean use_floating_btn;

    public SplashAdSetting(Integer show_duration, Boolean enable_close_on_click,
                           Boolean enable_full_click, Boolean invisible_ad_label, Boolean use_floating_btn) {
        this(show_duration, enable_close_on_click, enable_full_click, invisible_ad_label, use_floating_btn, ByteString.EMPTY);
    }

    public SplashAdSetting(Integer show_duration, Boolean enable_close_on_click,
                           Boolean enable_full_click, Boolean invisible_ad_label, Boolean use_floating_btn,
                           ByteString unknownFields) {
        super(ADAPTER, unknownFields);
        this.show_duration = show_duration;
        this.enable_close_on_click = enable_close_on_click;
        this.enable_full_click = enable_full_click;
        this.invisible_ad_label = invisible_ad_label;
        this.use_floating_btn = use_floating_btn;
    }

    @Override
    public Builder newBuilder() {
        Builder builder = new Builder();
        builder.show_duration = show_duration;
        builder.enable_close_on_click = enable_close_on_click;
        builder.enable_full_click = enable_full_click;
        builder.invisible_ad_label = invisible_ad_label;
        builder.use_floating_btn = use_floating_btn;
        builder.addUnknownFields(unknownFields());
        return builder;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SplashAdSetting)) return false;
        SplashAdSetting o = (SplashAdSetting) other;
        return unknownFields().equals(o.unknownFields())
                && Internal.equals(show_duration, o.show_duration)
                && Internal.equals(enable_close_on_click, o.enable_close_on_click)
                && Internal.equals(enable_full_click, o.enable_full_click)
                && Internal.equals(invisible_ad_label, o.invisible_ad_label)
                && Internal.equals(use_floating_btn, o.use_floating_btn);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode;
        if (result == 0) {
            result = unknownFields().hashCode();
            result = result * 37 + (show_duration != null ? show_duration.hashCode() : 0);
            result = result * 37 + (enable_close_on_click != null ? enable_close_on_click.hashCode() : 0);
            result = result * 37 + (enable_full_click != null ? enable_full_click.hashCode() : 0);
            result = result * 37 + (invisible_ad_label != null ? invisible_ad_label.hashCode() : 0);
            result = result * 37 + (use_floating_btn != null ? use_floating_btn.hashCode() : 0);
            super.hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (show_duration != null) builder.append(", show_duration=").append(show_duration);
        if (enable_close_on_click != null)
            builder.append(", enable_close_on_click=").append(enable_close_on_click);
        if (enable_full_click != null)
            builder.append(", enable_full_click=").append(enable_full_click);
        if (invisible_ad_label != null)
            builder.append(", invisible_ad_label=").append(invisible_ad_label);
        if (use_floating_btn != null)
            builder.append(", use_floating_btn=").append(use_floating_btn);
        return builder.replace(0, 2, "SplashAdSetting{").append('}').toString();
    }

    public static final class Builder extends Message.Builder<SplashAdSetting, Builder> {
        public Integer show_duration = DEFAULT_SHOW_DURATION;

        public Boolean enable_close_on_click = DEFAULT_ENABLE_CLOSE_ON_CLICK;

        public Boolean enable_full_click = DEFAULT_ENABLE_FULL_CLICK;

        public Boolean invisible_ad_label = DEFAULT_INVISIBLE_AD_LABEL;

        public Boolean use_floating_btn = DEFAULT_USE_FLOATING_BTN;

        public Builder() {
        }

        /**
         * 开屏倒计时时间,负值表示为定义，由sdk默认值决定（目前默认值是3）
         */
        public Builder show_duration(Integer show_duration) {
            this.show_duration = show_duration;
            return this;
        }

        /**
         * 开屏点击关闭开关，（false 关闭，true 不关闭，默认值 false)
         */
        public Builder enable_close_on_click(Boolean enable_close_on_click) {
            this.enable_close_on_click = enable_close_on_click;
            return this;
        }

        /**
         * 是否开启全屏点击，default=false
         */
        public Builder enable_full_click(Boolean enable_full_click) {
            this.enable_full_click = enable_full_click;
            return this;
        }

        /**
         * 是否不显示广告字样
         */
        public Builder invisible_ad_label(Boolean invisible_ad_label) {
            this.invisible_ad_label = invisible_ad_label;
            return this;
        }

        /**
         * 模版是否使用浮窗按钮（除摇一摇模版外）
         */
        public Builder use_floating_btn(Boolean use_floating_btn) {
            this.use_floating_btn = use_floating_btn;
            return this;
        }

        @Override
        public SplashAdSetting build() {
            return new SplashAdSetting(show_duration, enable_close_on_click, enable_full_click, invisible_ad_label, use_floating_btn, super.buildUnknownFields());
        }
    }

    private static final class ProtoAdapter_SplashAdSetting extends ProtoAdapter<SplashAdSetting> {
        public ProtoAdapter_SplashAdSetting() {
            super(FieldEncoding.LENGTH_DELIMITED, SplashAdSetting.class);
        }

        @Override
        public int encodedSize(SplashAdSetting value) {
            return ProtoAdapter.INT32.encodedSizeWithTag(1, value.show_duration)
                    + ProtoAdapter.BOOL.encodedSizeWithTag(2, value.enable_close_on_click)
                    + ProtoAdapter.BOOL.encodedSizeWithTag(5, value.enable_full_click)
                    + ProtoAdapter.BOOL.encodedSizeWithTag(6, value.invisible_ad_label)
                    + ProtoAdapter.BOOL.encodedSizeWithTag(7, value.use_floating_btn)
                    + value.unknownFields().size();
        }

        @Override
        public void encode(ProtoWriter writer, SplashAdSetting value) throws IOException {
            ProtoAdapter.INT32.encodeWithTag(writer, 1, value.show_duration);
            ProtoAdapter.BOOL.encodeWithTag(writer, 2, value.enable_close_on_click);
            ProtoAdapter.BOOL.encodeWithTag(writer, 5, value.enable_full_click);
            ProtoAdapter.BOOL.encodeWithTag(writer, 6, value.invisible_ad_label);
            ProtoAdapter.BOOL.encodeWithTag(writer, 7, value.use_floating_btn);
            writer.writeBytes(value.unknownFields());
        }

        @Override
        public SplashAdSetting decode(ProtoReader reader) throws IOException {
            Builder builder = new Builder();
            long token = reader.beginMessage();
            for (int tag; (tag = reader.nextTag()) != -1; ) {
                switch (tag) {
                    case 1:
                        builder.show_duration(ProtoAdapter.INT32.decode(reader));
                        break;
                    case 2:
                        builder.enable_close_on_click(ProtoAdapter.BOOL.decode(reader));
                        break;
                    case 5:
                        builder.enable_full_click(ProtoAdapter.BOOL.decode(reader));
                        break;
                    case 6:
                        builder.invisible_ad_label(ProtoAdapter.BOOL.decode(reader));
                        break;
                    case 7:
                        builder.use_floating_btn(ProtoAdapter.BOOL.decode(reader));
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
        public SplashAdSetting redact(SplashAdSetting value) {
            Builder builder = value.newBuilder();
            builder.clearUnknownFields();
            return builder.build();
        }
    }
}