<template>
    <div class="circle-editor">
        <div class="map-container" ref="mapContainerRef"></div>

        <!-- 半径输入框 -->
        <div class="radius-control">
            <label for="radius-input">{{ $t('半径（公里）:') }}</label>
            <input
                id="radius-input"
                v-model.number="inputRadius"
                type="number"
                class="radius-input"
                @change="updateCircleRadius"
                :placeholder="$t('请输入半径')"
                step="0.1"
            />
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import AMapLoader from "@amap/amap-jsapi-loader";
import { getConfigAmapAllSettings } from "@/api/setting/config";

// 定义响应式引用
const loading = ref(true);
const mapContainerRef = ref(null);
const map = ref(null);
const circle = ref(null);
const circleEditor = ref(null);
const AMap = ref(null);
const inputRadius = ref(1); // 默认半径1公里
const fillColor = ref('#1791fc'); // 默认填充颜色
const strokeColor = ref('#FF33FF'); // 默认边框颜色

// 组件属性
const props = defineProps({
    center: {
        type: Array,
        default: () => [116.433322, 39.900256]
    },
    radius: {
        type: Number,
        default: 1000
    }
});

// 调整地图视野以适应圆形
const fitMapView = () => {
    if (map.value && circle.value) {
        // 添加一个小延迟确保圆形已经更新
        setTimeout(() => {
            map.value.setFitView([circle.value]);
        }, 100);
    }
};

// 更新圆形半径
const updateCircleRadius = () => {
    if (circle.value && typeof inputRadius.value === "number" && inputRadius.value > 0) {
        // 将公里转换为米
        const radiusInMeters = inputRadius.value * 1000;
        circle.value.setRadius(radiusInMeters);
        
        // 重新设置圆形编辑器以更新控制点位置
        if (circleEditor.value) {
            circleEditor.value.close();
            circleEditor.value.open();
        }
        
        fitMapView();
    }
};

// 初始化地图
const initMap = async (initData) => {
    try {
        // 设置安全密钥
        window._AMapSecurityConfig = {
            securityJsCode: initData.amapJsSecret
        };

        // 加载高德地图API
        const loadedAMap = await AMapLoader.load({
            key: initData.amapJsKey,
            version: "2.0",
            plugins: ["AMap.CircleEditor"]
        });

        AMap.value = loadedAMap;

        // 创建地图实例
        map.value = new AMap.value.Map(mapContainerRef.value, {
            center: props.center,
            zoom: 14
        });

        // 设置初始半径值（将米转换为公里显示在输入框中）
        inputRadius.value = props.radius / 1000;

        // 创建圆形覆盖物
        circle.value = new AMap.value.Circle({
            center: props.center,
            radius: props.radius,
            borderWeight: 3,
            strokeColor: "#FF33FF",
            strokeOpacity: 1,
            strokeWeight: 6,
            strokeOpacity: 0.2,
            fillOpacity: 0.3,
            strokeStyle: "dashed",
            strokeDasharray: [10, 10],
            // 线样式还支持 'dashed'
            fillColor: "#628fdd",
            zIndex: 50
        });

        map.value.add(circle.value);
        map.value.setFitView([circle.value]);

        // 创建圆形编辑器并直接打开
        circleEditor.value = new AMap.value.CircleEditor(map.value, circle.value);
        circleEditor.value.open(); // 默认开启编辑模式

        // 绑定事件
        circleEditor.value.on("move", function (event) {
            console.log("触发事件：move");
        });

        circleEditor.value.on("adjust", function (event) {
            console.log("触发事件：adjust");
            // 当圆形调整时，更新输入框的值（将米转换为公里）
            if (circle.value) {
                inputRadius.value = circle.value.getRadius() / 1000;
            }
            // 当圆形调整时，重新设置地图视野
            fitMapView();
        });

        circleEditor.value.on("end", function (event) {
            console.log("触发事件：end");
            // event.target 即为编辑后的圆形对象
        });
    } catch (error) {
        console.error("地图加载失败:", error);
    }
};

const loadFilter = async () => {
    try {
        const result = await getConfigAmapAllSettings({ use: 1 });
        initMap(result);
    } catch (error) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadFilter();
});

onUnmounted(() => {
    // 清理地图实例
    if (map.value) {
        map.value.destroy();
    }
});

// 暴露方法给父组件
defineExpose({
    // 可以暴露其他需要的方法
});
</script>

<style scoped>
.circle-editor {
    width: 100%;
    height: 350px;
    position: relative;
}

.map-container {
    width: 100%;
    height: 100%;
}

.radius-control {
    position: absolute;
    top: 10px;
    left: 10px;
    background: rgba(255, 255, 255, 0.8);
    padding: 10px;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    z-index: 10;
    backdrop-filter: blur(5px);
}

.radius-control label {
    display: block;
    margin-bottom: 5px;
    font-size: 14px;
    color: #333;
}

.radius-input {
    width: 120px;
    padding: 6px 8px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    font-size: 14px;
}

.radius-input:focus {
    outline: none;
    border-color: #40a9ff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}
</style>
