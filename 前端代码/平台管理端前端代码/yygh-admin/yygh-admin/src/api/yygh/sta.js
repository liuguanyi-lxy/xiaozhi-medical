import request from '@/utils/request'
const api_name = '/admin/statistics'
export default {
    getCountMap(searchObj) {
        return request({
            url: `${api_name}/getCountMap`,
            method: 'get',
            params: searchObj // params 将json的数据以路径 问号 键值对形式传递   不用@RequestBody
            //data: searchObj // data 将json以请求体方式传递。 @RequestBody
        })
    }
}
