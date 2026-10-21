import type { App } from 'vue';
import {
    Input, Button, Space, ConfigProvider, Table, Tabs, Select, Divider, Switch, Spin, Dropdown, Form, Radio, Checkbox,
    Menu, Pagination, Alert, Tooltip, Popconfirm,Upload
} from 'ant-design-vue';
import PerfectScrollbar from 'vue3-perfect-scrollbar'
import 'vue3-perfect-scrollbar/dist/vue3-perfect-scrollbar.css'
export function regGlobalComponets(app: App) {
    app.use(Button) //1
        .use(Input)
        .use(Space)
        .use(ConfigProvider)
        .use(Table)
        .use(Tabs)
        .use(Select)
        .use(Divider)
        .use(Switch)
        .use(Spin) //1
        .use(Dropdown) //1
        .use(Form)
        .use(Radio)
        .use(Checkbox)
        .use(Menu)
        .use(Pagination)
        .use(Alert)
        .use(Popconfirm)
        .use(Tooltip)
        .use(Upload)
        .use(PerfectScrollbar)
}
