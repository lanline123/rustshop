use serde::Deserialize;

/// 后台开关/字段更新组件经常把 `val` 发成 boolean/number；
/// 统一接受并转成字符串，避免 47 个 updateField 接口重复 422。
pub fn deserialize_stringish<'de, D>(deserializer: D) -> Result<String, D::Error>
where
    D: serde::Deserializer<'de>,
{
    let value = serde_json::Value::deserialize(deserializer)?;
    Ok(value_to_string(value))
}

pub fn deserialize_opt_stringish<'de, D>(deserializer: D) -> Result<Option<String>, D::Error>
where
    D: serde::Deserializer<'de>,
{
    let value = Option::<serde_json::Value>::deserialize(deserializer)?;
    Ok(value.map(value_to_string))
}

fn value_to_string(value: serde_json::Value) -> String {
    match value {
        serde_json::Value::String(s) => s,
        serde_json::Value::Bool(b) => (if b { "1" } else { "0" }).to_string(),
        serde_json::Value::Number(n) => n.to_string(),
        serde_json::Value::Null => String::new(),
        other => other.to_string(),
    }
}
