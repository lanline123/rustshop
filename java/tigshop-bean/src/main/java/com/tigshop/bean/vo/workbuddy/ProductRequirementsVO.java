package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品导入的字段规范与调用约定。
 *
 * <p>调用方多为 AI 客户端，只能依据本结构生成入参，无法查阅文档，
 * 故除字段类型外还需给出约束、默认值、示例、调用顺序和不支持项。
 */
@Data
@Schema(description = "WorkBuddy商品导入规范")
public class ProductRequirementsVO {

    private String version = "v1";

    private List<Field> fields = new ArrayList<>();

    /** 调用顺序，避免调用方跳过校验直接导入。 */
    private List<String> workflow = new ArrayList<>();

    /** 本期不支持的能力，明示以免调用方反复尝试。 */
    private List<String> unsupported = new ArrayList<>();

    /**
     * 历史兼容字段。自然语言入口不再返回该字段；为空时序列化时省略。
     */
    @Deprecated
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Template template;

    /**
     * 可复制填写的商品模板。
     *
     * <p>空白件与样例件成对存在：只给空白件，用户不知道分类、规格该怎么写；
     * 只给样例件，用户会直接在样例上改，留下没删净的示例值。
     */
    @Data
    @Schema(description = "可填写的商品模板")
    public static class Template {

        /** 空白件，用户复制后逐行填写。 */
        private String blank;

        /** 样例件，同一份模板填好的示例，用于示范写法。 */
        private String example;
    }

    /**
     * 字段在用户模板中的归属。
     *
     * <p>与 {@code required} 分开表达：{@code required} 面向 AI 描述 API 契约，
     * 本枚举面向人描述填写表单。二者并非总是一致，例如分类在契约上是
     * categoryId 与 categoryName 二选一，在表单上只应出现一行「商品分类」。
     */
    public enum TemplateSection {

        /** 必填区。 */
        REQUIRED,

        /** 选填区，用户可整行删除。 */
        OPTIONAL,

        /** 不进模板，如仅供程序调用方使用的字段。 */
        OMITTED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "字段规范")
    public static class Field {

        private String name;

        /** 中文名，用作模板的行首标签。 */
        private String label;

        private String type;

        private String description;

        /** 取值约束，如长度上限、取值范围、唯一性要求。 */
        private String constraint;

        /** 未提供时服务端采用的值。 */
        private String defaultValue;

        private String example;

        private boolean required;

        /** 为空时按 {@code required} 推导，使新增字段默认进入模板而非被静默遗漏。 */
        private TemplateSection templateSection;

        /**
         * 模板中的示例值，缺省回落到 {@link #example}。
         *
         * <p>数组与对象字段需要单独给：{@code example} 是 JSON 写法，
         * 而模板是给人手写的，两者形态不同。
         */
        private String templateExample;

        /** 模板行尾的填写提示，仅在写法不显然时给出。 */
        private String templateHint;

        public TemplateSection sectionOrDefault() {
            if (templateSection != null) {
                return templateSection;
            }
            return required ? TemplateSection.REQUIRED : TemplateSection.OPTIONAL;
        }
    }
}
