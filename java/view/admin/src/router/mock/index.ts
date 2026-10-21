import { MockMethod } from "vite-plugin-mock"
 
const mock: Array<MockMethod> = [
    {
        // 接口路径
        url: '/apiss/brand/list/',
        
        // 接口方法
        method: 'get',
 
        // 返回数据
        response: () => {
            return {
                'abc':1231
            }
        }
    },
    {
        // 接口路径
        url: '/api/12brand/list/',
        
        // 接口方法
        method: 'get',
 
        // 返回数据
        response: () => {
            return {}
        }
    },
]
 
export default mock