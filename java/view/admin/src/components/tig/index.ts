import type { App } from 'vue';
import TigInput from './src/Input.vue';
import TigTabs from './src/Tabs.vue';

export function tigGlobalComponets(app: App) {
    app.component('TigInput', TigInput)
    app.component('TigTabs', TigTabs)
}
