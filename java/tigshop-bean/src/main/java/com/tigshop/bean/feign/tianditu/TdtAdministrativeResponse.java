package com.tigshop.bean.feign.tianditu;

import lombok.Data;

import java.util.List;

/**
 * 天地图行政区划响应（{@code /v2/administrative}）。
 * <p>
 * 成功判定为 {@code status} 等于 {@code 200}（数字）。查不到结果时 {@code status} 仍为 {@code 200}，
 * 仅 {@code data.district} 为空数组——不存在旧版 {@code returncode=101} 那种空结果码，
 * 故上层的行政层级回退须以「{@code district} 为空或取不到 {@code center}」为触发条件，
 * 见 CR-20260803-0925 §C4。
 *
 * @author Tigshop团队
 */
@Data
public class TdtAdministrativeResponse {

    private String message;

    /** 200 表示正常 */
    private Integer status;

    private AdministrativeData data;

    @Data
    public static class AdministrativeData {

        /** 模糊匹配到多条时的建议搜索词；精确命中时为空 */
        private List<String> suggestion;

        private List<District> district;

        @Data
        public static class District {

            private String name;

            /** 9 位国标码，形如 156110000 */
            private String gb;

            private String boundary;

            private Center center;

            /** 国家 5、省 4、市 3、区县 2 */
            private Integer level;

            private List<District> children;

            @Data
            public static class Center {
                private Double lng;
                private Double lat;
            }
        }
    }
}
