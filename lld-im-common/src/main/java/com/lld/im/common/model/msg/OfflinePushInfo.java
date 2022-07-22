package com.lld.im.common.model.msg;

import lombok.Data;

@Data
public class OfflinePushInfo {

    @Data
    public static class AndroidInfo {
        /**
         * Android 离线推送声音文件路径
         */
        private String sound;
        /**
         * OPPO 手机 Android 8.0 以上的 NotificationChannel 通知适配字段
         */
        private String oppoChannelID;

        @Override
        public String toString() {
            return "AndroidInfo{" +
                    "sound='" + sound + '\'' +
                    ", oppoChannelID='" + oppoChannelID + '\'' +
                    '}';
        }
    }


    @Data
    public static class ApnsInfo {
        /**
         * Android 离线推送声音文件路径
         */
        private String sound;
        /**
         * 这个字段缺省或者为0表示需要计数，为1表示本条消息不需要计数，即右上角图标数字不增加
         */
        private int badgeMode;
        /**
         * 该字段用于标识 APNs 推送的标题，若填写则会覆盖最上层 Title
         */
        private String title;
        /**
         * 该字段用于标识 APNs 推送的子标题
         */
        private String subTitle;
        /**
         * 该字段用于标识 APNs 携带的图片地址，当客户端拿到该字段时，可以通过下载图片资源的方式将图片展示在弹窗上
         */
        private String image;


        @Override
        public String toString() {
            return "ApnsInfo{" +
                    "sound='" + sound + '\'' +
                    ", badgeMode=" + badgeMode +
                    ", title='" + title + '\'' +
                    ", subTitle='" + subTitle + '\'' +
                    ", image='" + image + '\'' +
                    '}';
        }
    }


    /**
     * 选填  0表示推送，1表示不离线推送
     */
    private int pushFlag;
    /**
     * 离线推送标题。该字段为 iOS 和 Android 共用
     */
    private String title;
    /**
     * 离线推送内容。该字段会覆盖上面各种消息默认的离线推送展示文本。
     * 若发送的消息只有一个 MIMCustomMsg 自定义消息元素，
     * 该 text字段会覆盖 MIMCustomMsg 中的 text字段。
     * 如果两个 text字段都不填，将收不到该自定义消息的离线推送。
     */
    private String text;
    /**
     * 离线推送透传内容。由于国内各 Android 手机厂商的推送平台要求各不一样，请保证此字段为 JSON 格式，否则可能会导致收不到某些厂商的离线推送。
     */
    private String ext;

    private AndroidInfo androidInfo;

    private ApnsInfo apnsInfo;

    @Override
    public String toString() {
        return "OfflinePushInfo{" +
                "pushFlag=" + pushFlag +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", ext='" + ext + '\'' +
                ", androidInfo=" + androidInfo +
                ", apnsInfo=" + apnsInfo +
                '}';
    }


}
