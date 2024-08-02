// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: sigmob_common.proto
package com.czhj.sdk.common.models;

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

public final class Device extends AndroidMessage<Device, Device.Builder> {
  public static final ProtoAdapter<Device> ADAPTER = new ProtoAdapter_Device();

  public static final Parcelable.Creator<Device> CREATOR = AndroidMessage.newCreator(ADAPTER);

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_DEVICE_TYPE = 0;

  public static final Integer DEFAULT_OS_TYPE = 0;

  public static final String DEFAULT_VENDOR = "";

  public static final String DEFAULT_MODEL = "";

  public static final Integer DEFAULT_DPI = 0;

  public static final Boolean DEFAULT_IS_ROOT = false;

  public static final Long DEFAULT_DISK_SIZE = 0L;

  public static final Integer DEFAULT_BATTERY_STATE = 0;

  public static final Float DEFAULT_BATTERY_LEVEL = 0.0f;

  public static final Boolean DEFAULT_BATTERY_SAVE_ENABLED = false;

  public static final String DEFAULT_DEVICE_NAME = "";

  public static final Long DEFAULT_START_TIMESTAMP = 0L;

  public static final Integer DEFAULT_ANDROID_API_LEVEL = 0;

  public static final Long DEFAULT_MEM_SIZE = 0L;

  public static final Long DEFAULT_TOTAL_DISK_SIZE = 0L;

  public static final Long DEFAULT_FREE_DISK_SIZE = 0L;

  public static final Long DEFAULT_SD_TOTAL_DISK_SIZE = 0L;

  public static final Long DEFAULT_SD_FREE_DISK_SIZE = 0L;

  public static final String DEFAULT_SYSTEM_UPDATE_TIME = "";

  public static final String DEFAULT_INTERNAL_NAME = "";

  public static final String DEFAULT_BOOT_MARK = "";

  public static final String DEFAULT_UPDATE_MARK = "";

  public static final String DEFAULT_ROM_NAME = "";

  public static final Integer DEFAULT_MARKET_VERSION = 0;

  public static final Integer DEFAULT_HMS_VERSION = 0;

  /**
   * 设备类型。0:unknown、1:iPhone、2:iPad、3:iPod、4:Android phone、5:Android pad
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer device_type;

  /**
   * 操作系统类型. 1=IOS；2=Android
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer os_type;

  /**
   * 必填！操作系统版本
   */
  @WireField(
      tag = 3,
      adapter = "com.sigmob.sdk.common.models.Version#ADAPTER"
  )
  public final Version os_version;

  /**
   * 必填！设备厂商名称，中文需要UTF-8编码
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String vendor;

  /**
   * 必填！设备型号，中文需要UTF-8编码
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String model;

  /**
   * 必填！唯一设备标识，必需按要求填写
   */
  @WireField(
      tag = 6,
      adapter = "com.sigmob.sdk.common.models.DeviceId#ADAPTER"
  )
  public final DeviceId did;

  /**
   * 必填！设备屏幕宽高
   */
  @WireField(
      tag = 7,
      adapter = "com.sigmob.sdk.common.models.Size#ADAPTER"
  )
  public final Size screen_size;

  /**
   * 地理信息
   */
  @WireField(
      tag = 8,
      adapter = "com.sigmob.sdk.common.models.Geo#ADAPTER"
  )
  public final Geo geo;

  /**
   * 屏幕密度
   */
  @WireField(
      tag = 9,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer dpi;

  /**
   * 是否越狱（true：越狱）
   */
  @WireField(
      tag = 10,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  public final Boolean is_root;

  /**
   * 磁盘大小（单位Byte）【已废弃】
   */
  @WireField(
      tag = 11,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long disk_size;

  /**
   * 电池充电的状态（0=UnKnow、1=Unplugged、2=Charging、3=Full）
   */
  @WireField(
      tag = 13,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer battery_state;

  /**
   * 电池电量百分比
   */
  @WireField(
      tag = 14,
      adapter = "com.squareup.wire.ProtoAdapter#FLOAT"
  )
  public final Float battery_level;

  /**
   * 是否开启低电量模式
   */
  @WireField(
      tag = 15,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  public final Boolean battery_save_enabled;

  /**
   * 设备名称
   */
  @WireField(
      tag = 16,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String device_name;

  /**
   * 设备启动时间,unix时间戳 (10 位，从1970开始的时间戳)
   */
  @WireField(
      tag = 17,
      adapter = "com.squareup.wire.ProtoAdapter#INT64"
  )
  public final Long start_timestamp;

  /**
   * Android API level
   */
  @WireField(
      tag = 18,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  public final Integer android_api_level;

  /**
   * 系统内存大小，安卓必填（单位Byte）
   */
  @WireField(
      tag = 19,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long mem_size;

  /**
   * 手机磁盘总大小（单位Byte）
   */
  @WireField(
      tag = 20,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long total_disk_size;

  /**
   * 手机磁盘剩余大小（单位Byte）
   */
  @WireField(
      tag = 21,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long free_disk_size;

  /**
   * 设备SD磁盘总大小（单位Byte）
   */
  @WireField(
      tag = 22,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long sd_total_disk_size;

  /**
   * 设备SD磁盘剩余大小（单位Byte）
   */
  @WireField(
      tag = 23,
      adapter = "com.squareup.wire.ProtoAdapter#UINT64"
  )
  public final Long sd_free_disk_size;

  /**
   * 设备分辨率（单位px）
   */
  @WireField(
      tag = 24,
      adapter = "com.sigmob.sdk.common.models.Size#ADAPTER"
  )
  public final Size resolution;

  /**
   * 系统更新的时间
   */
  @WireField(
      tag = 25,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String system_update_time;

  /**
   * 手机mode编码
   */
  @WireField(
      tag = 26,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String internal_name;

  /**
   * 手机重启时间
   */
  @WireField(
      tag = 27,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String boot_mark;

  /**
   * 手机系统更新时间
   */
  @WireField(
      tag = 28,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String update_mark;

  /**
   * 厂商定制化系统ROM名称(MIUI,EMUI等)
   */
  @WireField(
      tag = 32,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String rom_name;

  /**
   * 厂商定制化系统系统ROM版本号（非Android 版本号）
   */
  @WireField(
      tag = 33,
      adapter = "com.sigmob.sdk.common.models.Version#ADAPTER"
  )
  public final Version rom_version;

  /**
   * 应用市场app versionCode                                                                                                                                                                                                                                          //厂商应用市场market 版本号
   */
  @WireField(
      tag = 34,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer market_version;

  /**
   * 华为Hms Core VersionCode
   */
  @WireField(
      tag = 35,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer hms_version;

  public Device(Integer device_type, Integer os_type, Version os_version, String vendor,
      String model, DeviceId did, Size screen_size, Geo geo, Integer dpi, Boolean is_root,
      Long disk_size, Integer battery_state, Float battery_level, Boolean battery_save_enabled,
      String device_name, Long start_timestamp, Integer android_api_level, Long mem_size,
      Long total_disk_size, Long free_disk_size, Long sd_total_disk_size, Long sd_free_disk_size,
      Size resolution, String system_update_time, String internal_name, String boot_mark,
      String update_mark, String rom_name, Version rom_version, Integer market_version,
      Integer hms_version) {
    this(device_type, os_type, os_version, vendor, model, did, screen_size, geo, dpi, is_root, disk_size, battery_state, battery_level, battery_save_enabled, device_name, start_timestamp, android_api_level, mem_size, total_disk_size, free_disk_size, sd_total_disk_size, sd_free_disk_size, resolution, system_update_time, internal_name, boot_mark, update_mark, rom_name, rom_version, market_version, hms_version, ByteString.EMPTY);
  }

  public Device(Integer device_type, Integer os_type, Version os_version, String vendor,
      String model, DeviceId did, Size screen_size, Geo geo, Integer dpi, Boolean is_root,
      Long disk_size, Integer battery_state, Float battery_level, Boolean battery_save_enabled,
      String device_name, Long start_timestamp, Integer android_api_level, Long mem_size,
      Long total_disk_size, Long free_disk_size, Long sd_total_disk_size, Long sd_free_disk_size,
      Size resolution, String system_update_time, String internal_name, String boot_mark,
      String update_mark, String rom_name, Version rom_version, Integer market_version,
      Integer hms_version, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.device_type = device_type;
    this.os_type = os_type;
    this.os_version = os_version;
    this.vendor = vendor;
    this.model = model;
    this.did = did;
    this.screen_size = screen_size;
    this.geo = geo;
    this.dpi = dpi;
    this.is_root = is_root;
    this.disk_size = disk_size;
    this.battery_state = battery_state;
    this.battery_level = battery_level;
    this.battery_save_enabled = battery_save_enabled;
    this.device_name = device_name;
    this.start_timestamp = start_timestamp;
    this.android_api_level = android_api_level;
    this.mem_size = mem_size;
    this.total_disk_size = total_disk_size;
    this.free_disk_size = free_disk_size;
    this.sd_total_disk_size = sd_total_disk_size;
    this.sd_free_disk_size = sd_free_disk_size;
    this.resolution = resolution;
    this.system_update_time = system_update_time;
    this.internal_name = internal_name;
    this.boot_mark = boot_mark;
    this.update_mark = update_mark;
    this.rom_name = rom_name;
    this.rom_version = rom_version;
    this.market_version = market_version;
    this.hms_version = hms_version;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.device_type = device_type;
    builder.os_type = os_type;
    builder.os_version = os_version;
    builder.vendor = vendor;
    builder.model = model;
    builder.did = did;
    builder.screen_size = screen_size;
    builder.geo = geo;
    builder.dpi = dpi;
    builder.is_root = is_root;
    builder.disk_size = disk_size;
    builder.battery_state = battery_state;
    builder.battery_level = battery_level;
    builder.battery_save_enabled = battery_save_enabled;
    builder.device_name = device_name;
    builder.start_timestamp = start_timestamp;
    builder.android_api_level = android_api_level;
    builder.mem_size = mem_size;
    builder.total_disk_size = total_disk_size;
    builder.free_disk_size = free_disk_size;
    builder.sd_total_disk_size = sd_total_disk_size;
    builder.sd_free_disk_size = sd_free_disk_size;
    builder.resolution = resolution;
    builder.system_update_time = system_update_time;
    builder.internal_name = internal_name;
    builder.boot_mark = boot_mark;
    builder.update_mark = update_mark;
    builder.rom_name = rom_name;
    builder.rom_version = rom_version;
    builder.market_version = market_version;
    builder.hms_version = hms_version;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Device)) return false;
    Device o = (Device) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(device_type, o.device_type)
        && Internal.equals(os_type, o.os_type)
        && Internal.equals(os_version, o.os_version)
        && Internal.equals(vendor, o.vendor)
        && Internal.equals(model, o.model)
        && Internal.equals(did, o.did)
        && Internal.equals(screen_size, o.screen_size)
        && Internal.equals(geo, o.geo)
        && Internal.equals(dpi, o.dpi)
        && Internal.equals(is_root, o.is_root)
        && Internal.equals(disk_size, o.disk_size)
        && Internal.equals(battery_state, o.battery_state)
        && Internal.equals(battery_level, o.battery_level)
        && Internal.equals(battery_save_enabled, o.battery_save_enabled)
        && Internal.equals(device_name, o.device_name)
        && Internal.equals(start_timestamp, o.start_timestamp)
        && Internal.equals(android_api_level, o.android_api_level)
        && Internal.equals(mem_size, o.mem_size)
        && Internal.equals(total_disk_size, o.total_disk_size)
        && Internal.equals(free_disk_size, o.free_disk_size)
        && Internal.equals(sd_total_disk_size, o.sd_total_disk_size)
        && Internal.equals(sd_free_disk_size, o.sd_free_disk_size)
        && Internal.equals(resolution, o.resolution)
        && Internal.equals(system_update_time, o.system_update_time)
        && Internal.equals(internal_name, o.internal_name)
        && Internal.equals(boot_mark, o.boot_mark)
        && Internal.equals(update_mark, o.update_mark)
        && Internal.equals(rom_name, o.rom_name)
        && Internal.equals(rom_version, o.rom_version)
        && Internal.equals(market_version, o.market_version)
        && Internal.equals(hms_version, o.hms_version);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (device_type != null ? device_type.hashCode() : 0);
      result = result * 37 + (os_type != null ? os_type.hashCode() : 0);
      result = result * 37 + (os_version != null ? os_version.hashCode() : 0);
      result = result * 37 + (vendor != null ? vendor.hashCode() : 0);
      result = result * 37 + (model != null ? model.hashCode() : 0);
      result = result * 37 + (did != null ? did.hashCode() : 0);
      result = result * 37 + (screen_size != null ? screen_size.hashCode() : 0);
      result = result * 37 + (geo != null ? geo.hashCode() : 0);
      result = result * 37 + (dpi != null ? dpi.hashCode() : 0);
      result = result * 37 + (is_root != null ? is_root.hashCode() : 0);
      result = result * 37 + (disk_size != null ? disk_size.hashCode() : 0);
      result = result * 37 + (battery_state != null ? battery_state.hashCode() : 0);
      result = result * 37 + (battery_level != null ? battery_level.hashCode() : 0);
      result = result * 37 + (battery_save_enabled != null ? battery_save_enabled.hashCode() : 0);
      result = result * 37 + (device_name != null ? device_name.hashCode() : 0);
      result = result * 37 + (start_timestamp != null ? start_timestamp.hashCode() : 0);
      result = result * 37 + (android_api_level != null ? android_api_level.hashCode() : 0);
      result = result * 37 + (mem_size != null ? mem_size.hashCode() : 0);
      result = result * 37 + (total_disk_size != null ? total_disk_size.hashCode() : 0);
      result = result * 37 + (free_disk_size != null ? free_disk_size.hashCode() : 0);
      result = result * 37 + (sd_total_disk_size != null ? sd_total_disk_size.hashCode() : 0);
      result = result * 37 + (sd_free_disk_size != null ? sd_free_disk_size.hashCode() : 0);
      result = result * 37 + (resolution != null ? resolution.hashCode() : 0);
      result = result * 37 + (system_update_time != null ? system_update_time.hashCode() : 0);
      result = result * 37 + (internal_name != null ? internal_name.hashCode() : 0);
      result = result * 37 + (boot_mark != null ? boot_mark.hashCode() : 0);
      result = result * 37 + (update_mark != null ? update_mark.hashCode() : 0);
      result = result * 37 + (rom_name != null ? rom_name.hashCode() : 0);
      result = result * 37 + (rom_version != null ? rom_version.hashCode() : 0);
      result = result * 37 + (market_version != null ? market_version.hashCode() : 0);
      result = result * 37 + (hms_version != null ? hms_version.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (device_type != null) builder.append(", device_type=").append(device_type);
    if (os_type != null) builder.append(", os_type=").append(os_type);
    if (os_version != null) builder.append(", os_version=").append(os_version);
    if (vendor != null) builder.append(", vendor=").append(vendor);
    if (model != null) builder.append(", model=").append(model);
    if (did != null) builder.append(", did=").append(did);
    if (screen_size != null) builder.append(", screen_size=").append(screen_size);
    if (geo != null) builder.append(", geo=").append(geo);
    if (dpi != null) builder.append(", dpi=").append(dpi);
    if (is_root != null) builder.append(", is_root=").append(is_root);
    if (disk_size != null) builder.append(", disk_size=").append(disk_size);
    if (battery_state != null) builder.append(", battery_state=").append(battery_state);
    if (battery_level != null) builder.append(", battery_level=").append(battery_level);
    if (battery_save_enabled != null) builder.append(", battery_save_enabled=").append(battery_save_enabled);
    if (device_name != null) builder.append(", device_name=").append(device_name);
    if (start_timestamp != null) builder.append(", start_timestamp=").append(start_timestamp);
    if (android_api_level != null) builder.append(", android_api_level=").append(android_api_level);
    if (mem_size != null) builder.append(", mem_size=").append(mem_size);
    if (total_disk_size != null) builder.append(", total_disk_size=").append(total_disk_size);
    if (free_disk_size != null) builder.append(", free_disk_size=").append(free_disk_size);
    if (sd_total_disk_size != null) builder.append(", sd_total_disk_size=").append(sd_total_disk_size);
    if (sd_free_disk_size != null) builder.append(", sd_free_disk_size=").append(sd_free_disk_size);
    if (resolution != null) builder.append(", resolution=").append(resolution);
    if (system_update_time != null) builder.append(", system_update_time=").append(system_update_time);
    if (internal_name != null) builder.append(", internal_name=").append(internal_name);
    if (boot_mark != null) builder.append(", boot_mark=").append(boot_mark);
    if (update_mark != null) builder.append(", update_mark=").append(update_mark);
    if (rom_name != null) builder.append(", rom_name=").append(rom_name);
    if (rom_version != null) builder.append(", rom_version=").append(rom_version);
    if (market_version != null) builder.append(", market_version=").append(market_version);
    if (hms_version != null) builder.append(", hms_version=").append(hms_version);
    return builder.replace(0, 2, "Device{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<Device, Builder> {
    public Integer device_type;

    public Integer os_type;

    public Version os_version;

    public String vendor;

    public String model;

    public DeviceId did;

    public Size screen_size;

    public Geo geo;

    public Integer dpi;

    public Boolean is_root;

    public Long disk_size;

    public Integer battery_state;

    public Float battery_level;

    public Boolean battery_save_enabled;

    public String device_name;

    public Long start_timestamp;

    public Integer android_api_level;

    public Long mem_size;

    public Long total_disk_size;

    public Long free_disk_size;

    public Long sd_total_disk_size;

    public Long sd_free_disk_size;

    public Size resolution;

    public String system_update_time;

    public String internal_name;

    public String boot_mark;

    public String update_mark;

    public String rom_name;

    public Version rom_version;

    public Integer market_version;

    public Integer hms_version;

    public Builder() {
    }

    /**
     * 设备类型。0:unknown、1:iPhone、2:iPad、3:iPod、4:Android phone、5:Android pad
     */
    public Builder device_type(Integer device_type) {
      this.device_type = device_type;
      return this;
    }

    /**
     * 操作系统类型. 1=IOS；2=Android
     */
    public Builder os_type(Integer os_type) {
      this.os_type = os_type;
      return this;
    }

    /**
     * 必填！操作系统版本
     */
    public Builder os_version(Version os_version) {
      this.os_version = os_version;
      return this;
    }

    /**
     * 必填！设备厂商名称，中文需要UTF-8编码
     */
    public Builder vendor(String vendor) {
      this.vendor = vendor;
      return this;
    }

    /**
     * 必填！设备型号，中文需要UTF-8编码
     */
    public Builder model(String model) {
      this.model = model;
      return this;
    }

    /**
     * 必填！唯一设备标识，必需按要求填写
     */
    public Builder did(DeviceId did) {
      this.did = did;
      return this;
    }

    /**
     * 必填！设备屏幕宽高
     */
    public Builder screen_size(Size screen_size) {
      this.screen_size = screen_size;
      return this;
    }

    /**
     * 地理信息
     */
    public Builder geo(Geo geo) {
      this.geo = geo;
      return this;
    }

    /**
     * 屏幕密度
     */
    public Builder dpi(Integer dpi) {
      this.dpi = dpi;
      return this;
    }

    /**
     * 是否越狱（true：越狱）
     */
    public Builder is_root(Boolean is_root) {
      this.is_root = is_root;
      return this;
    }

    /**
     * 磁盘大小（单位Byte）【已废弃】
     */
    public Builder disk_size(Long disk_size) {
      this.disk_size = disk_size;
      return this;
    }

    /**
     * 电池充电的状态（0=UnKnow、1=Unplugged、2=Charging、3=Full）
     */
    public Builder battery_state(Integer battery_state) {
      this.battery_state = battery_state;
      return this;
    }

    /**
     * 电池电量百分比
     */
    public Builder battery_level(Float battery_level) {
      this.battery_level = battery_level;
      return this;
    }

    /**
     * 是否开启低电量模式
     */
    public Builder battery_save_enabled(Boolean battery_save_enabled) {
      this.battery_save_enabled = battery_save_enabled;
      return this;
    }

    /**
     * 设备名称
     */
    public Builder device_name(String device_name) {
      this.device_name = device_name;
      return this;
    }

    /**
     * 设备启动时间,unix时间戳 (10 位，从1970开始的时间戳)
     */
    public Builder start_timestamp(Long start_timestamp) {
      this.start_timestamp = start_timestamp;
      return this;
    }

    /**
     * Android API level
     */
    public Builder android_api_level(Integer android_api_level) {
      this.android_api_level = android_api_level;
      return this;
    }

    /**
     * 系统内存大小，安卓必填（单位Byte）
     */
    public Builder mem_size(Long mem_size) {
      this.mem_size = mem_size;
      return this;
    }

    /**
     * 手机磁盘总大小（单位Byte）
     */
    public Builder total_disk_size(Long total_disk_size) {
      this.total_disk_size = total_disk_size;
      return this;
    }

    /**
     * 手机磁盘剩余大小（单位Byte）
     */
    public Builder free_disk_size(Long free_disk_size) {
      this.free_disk_size = free_disk_size;
      return this;
    }

    /**
     * 设备SD磁盘总大小（单位Byte）
     */
    public Builder sd_total_disk_size(Long sd_total_disk_size) {
      this.sd_total_disk_size = sd_total_disk_size;
      return this;
    }

    /**
     * 设备SD磁盘剩余大小（单位Byte）
     */
    public Builder sd_free_disk_size(Long sd_free_disk_size) {
      this.sd_free_disk_size = sd_free_disk_size;
      return this;
    }

    /**
     * 设备分辨率（单位px）
     */
    public Builder resolution(Size resolution) {
      this.resolution = resolution;
      return this;
    }

    /**
     * 系统更新的时间
     */
    public Builder system_update_time(String system_update_time) {
      this.system_update_time = system_update_time;
      return this;
    }

    /**
     * 手机mode编码
     */
    public Builder internal_name(String internal_name) {
      this.internal_name = internal_name;
      return this;
    }

    /**
     * 手机重启时间
     */
    public Builder boot_mark(String boot_mark) {
      this.boot_mark = boot_mark;
      return this;
    }

    /**
     * 手机系统更新时间
     */
    public Builder update_mark(String update_mark) {
      this.update_mark = update_mark;
      return this;
    }

    /**
     * 厂商定制化系统ROM名称(MIUI,EMUI等)
     */
    public Builder rom_name(String rom_name) {
      this.rom_name = rom_name;
      return this;
    }

    /**
     * 厂商定制化系统系统ROM版本号（非Android 版本号）
     */
    public Builder rom_version(Version rom_version) {
      this.rom_version = rom_version;
      return this;
    }

    /**
     * 应用市场app versionCode                                                                                                                                                                                                                                          //厂商应用市场market 版本号
     */
    public Builder market_version(Integer market_version) {
      this.market_version = market_version;
      return this;
    }

    /**
     * 华为Hms Core VersionCode
     */
    public Builder hms_version(Integer hms_version) {
      this.hms_version = hms_version;
      return this;
    }

    @Override
    public Device build() {
      return new Device(device_type, os_type, os_version, vendor, model, did, screen_size, geo, dpi, is_root, disk_size, battery_state, battery_level, battery_save_enabled, device_name, start_timestamp, android_api_level, mem_size, total_disk_size, free_disk_size, sd_total_disk_size, sd_free_disk_size, resolution, system_update_time, internal_name, boot_mark, update_mark, rom_name, rom_version, market_version, hms_version, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_Device extends ProtoAdapter<Device> {
    public ProtoAdapter_Device() {
      super(FieldEncoding.LENGTH_DELIMITED, Device.class);
    }

    @Override
    public int encodedSize(Device value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.device_type)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.os_type)
          + Version.ADAPTER.encodedSizeWithTag(3, value.os_version)
          + ProtoAdapter.STRING.encodedSizeWithTag(4, value.vendor)
          + ProtoAdapter.STRING.encodedSizeWithTag(5, value.model)
          + DeviceId.ADAPTER.encodedSizeWithTag(6, value.did)
          + Size.ADAPTER.encodedSizeWithTag(7, value.screen_size)
          + Geo.ADAPTER.encodedSizeWithTag(8, value.geo)
          + ProtoAdapter.UINT32.encodedSizeWithTag(9, value.dpi)
          + ProtoAdapter.BOOL.encodedSizeWithTag(10, value.is_root)
          + ProtoAdapter.UINT64.encodedSizeWithTag(11, value.disk_size)
          + ProtoAdapter.UINT32.encodedSizeWithTag(13, value.battery_state)
          + ProtoAdapter.FLOAT.encodedSizeWithTag(14, value.battery_level)
          + ProtoAdapter.BOOL.encodedSizeWithTag(15, value.battery_save_enabled)
          + ProtoAdapter.STRING.encodedSizeWithTag(16, value.device_name)
          + ProtoAdapter.INT64.encodedSizeWithTag(17, value.start_timestamp)
          + ProtoAdapter.UINT32.encodedSizeWithTag(18, value.android_api_level)
          + ProtoAdapter.UINT64.encodedSizeWithTag(19, value.mem_size)
          + ProtoAdapter.UINT64.encodedSizeWithTag(20, value.total_disk_size)
          + ProtoAdapter.UINT64.encodedSizeWithTag(21, value.free_disk_size)
          + ProtoAdapter.UINT64.encodedSizeWithTag(22, value.sd_total_disk_size)
          + ProtoAdapter.UINT64.encodedSizeWithTag(23, value.sd_free_disk_size)
          + Size.ADAPTER.encodedSizeWithTag(24, value.resolution)
          + ProtoAdapter.STRING.encodedSizeWithTag(25, value.system_update_time)
          + ProtoAdapter.STRING.encodedSizeWithTag(26, value.internal_name)
          + ProtoAdapter.STRING.encodedSizeWithTag(27, value.boot_mark)
          + ProtoAdapter.STRING.encodedSizeWithTag(28, value.update_mark)
          + ProtoAdapter.STRING.encodedSizeWithTag(32, value.rom_name)
          + Version.ADAPTER.encodedSizeWithTag(33, value.rom_version)
          + ProtoAdapter.INT32.encodedSizeWithTag(34, value.market_version)
          + ProtoAdapter.INT32.encodedSizeWithTag(35, value.hms_version)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, Device value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.device_type);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.os_type);
      Version.ADAPTER.encodeWithTag(writer, 3, value.os_version);
      ProtoAdapter.STRING.encodeWithTag(writer, 4, value.vendor);
      ProtoAdapter.STRING.encodeWithTag(writer, 5, value.model);
      DeviceId.ADAPTER.encodeWithTag(writer, 6, value.did);
      Size.ADAPTER.encodeWithTag(writer, 7, value.screen_size);
      Geo.ADAPTER.encodeWithTag(writer, 8, value.geo);
      ProtoAdapter.UINT32.encodeWithTag(writer, 9, value.dpi);
      ProtoAdapter.BOOL.encodeWithTag(writer, 10, value.is_root);
      ProtoAdapter.UINT64.encodeWithTag(writer, 11, value.disk_size);
      ProtoAdapter.UINT32.encodeWithTag(writer, 13, value.battery_state);
      ProtoAdapter.FLOAT.encodeWithTag(writer, 14, value.battery_level);
      ProtoAdapter.BOOL.encodeWithTag(writer, 15, value.battery_save_enabled);
      ProtoAdapter.STRING.encodeWithTag(writer, 16, value.device_name);
      ProtoAdapter.INT64.encodeWithTag(writer, 17, value.start_timestamp);
      ProtoAdapter.UINT32.encodeWithTag(writer, 18, value.android_api_level);
      ProtoAdapter.UINT64.encodeWithTag(writer, 19, value.mem_size);
      ProtoAdapter.UINT64.encodeWithTag(writer, 20, value.total_disk_size);
      ProtoAdapter.UINT64.encodeWithTag(writer, 21, value.free_disk_size);
      ProtoAdapter.UINT64.encodeWithTag(writer, 22, value.sd_total_disk_size);
      ProtoAdapter.UINT64.encodeWithTag(writer, 23, value.sd_free_disk_size);
      Size.ADAPTER.encodeWithTag(writer, 24, value.resolution);
      ProtoAdapter.STRING.encodeWithTag(writer, 25, value.system_update_time);
      ProtoAdapter.STRING.encodeWithTag(writer, 26, value.internal_name);
      ProtoAdapter.STRING.encodeWithTag(writer, 27, value.boot_mark);
      ProtoAdapter.STRING.encodeWithTag(writer, 28, value.update_mark);
      ProtoAdapter.STRING.encodeWithTag(writer, 32, value.rom_name);
      Version.ADAPTER.encodeWithTag(writer, 33, value.rom_version);
      ProtoAdapter.INT32.encodeWithTag(writer, 34, value.market_version);
      ProtoAdapter.INT32.encodeWithTag(writer, 35, value.hms_version);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public Device decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.device_type(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.os_type(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.os_version(Version.ADAPTER.decode(reader)); break;
          case 4: builder.vendor(ProtoAdapter.STRING.decode(reader)); break;
          case 5: builder.model(ProtoAdapter.STRING.decode(reader)); break;
          case 6: builder.did(DeviceId.ADAPTER.decode(reader)); break;
          case 7: builder.screen_size(Size.ADAPTER.decode(reader)); break;
          case 8: builder.geo(Geo.ADAPTER.decode(reader)); break;
          case 9: builder.dpi(ProtoAdapter.UINT32.decode(reader)); break;
          case 10: builder.is_root(ProtoAdapter.BOOL.decode(reader)); break;
          case 11: builder.disk_size(ProtoAdapter.UINT64.decode(reader)); break;
          case 13: builder.battery_state(ProtoAdapter.UINT32.decode(reader)); break;
          case 14: builder.battery_level(ProtoAdapter.FLOAT.decode(reader)); break;
          case 15: builder.battery_save_enabled(ProtoAdapter.BOOL.decode(reader)); break;
          case 16: builder.device_name(ProtoAdapter.STRING.decode(reader)); break;
          case 17: builder.start_timestamp(ProtoAdapter.INT64.decode(reader)); break;
          case 18: builder.android_api_level(ProtoAdapter.UINT32.decode(reader)); break;
          case 19: builder.mem_size(ProtoAdapter.UINT64.decode(reader)); break;
          case 20: builder.total_disk_size(ProtoAdapter.UINT64.decode(reader)); break;
          case 21: builder.free_disk_size(ProtoAdapter.UINT64.decode(reader)); break;
          case 22: builder.sd_total_disk_size(ProtoAdapter.UINT64.decode(reader)); break;
          case 23: builder.sd_free_disk_size(ProtoAdapter.UINT64.decode(reader)); break;
          case 24: builder.resolution(Size.ADAPTER.decode(reader)); break;
          case 25: builder.system_update_time(ProtoAdapter.STRING.decode(reader)); break;
          case 26: builder.internal_name(ProtoAdapter.STRING.decode(reader)); break;
          case 27: builder.boot_mark(ProtoAdapter.STRING.decode(reader)); break;
          case 28: builder.update_mark(ProtoAdapter.STRING.decode(reader)); break;
          case 32: builder.rom_name(ProtoAdapter.STRING.decode(reader)); break;
          case 33: builder.rom_version(Version.ADAPTER.decode(reader)); break;
          case 34: builder.market_version(ProtoAdapter.INT32.decode(reader)); break;
          case 35: builder.hms_version(ProtoAdapter.INT32.decode(reader)); break;
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
    public Device redact(Device value) {
      Builder builder = value.newBuilder();
      if (builder.os_version != null) builder.os_version = Version.ADAPTER.redact(builder.os_version);
      if (builder.did != null) builder.did = DeviceId.ADAPTER.redact(builder.did);
      if (builder.screen_size != null) builder.screen_size = Size.ADAPTER.redact(builder.screen_size);
      if (builder.geo != null) builder.geo = Geo.ADAPTER.redact(builder.geo);
      if (builder.resolution != null) builder.resolution = Size.ADAPTER.redact(builder.resolution);
      if (builder.rom_version != null) builder.rom_version = Version.ADAPTER.redact(builder.rom_version);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}