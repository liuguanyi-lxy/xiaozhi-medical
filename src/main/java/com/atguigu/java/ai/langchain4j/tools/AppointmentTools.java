package com.atguigu.java.ai.langchain4j.tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.java.ai.langchain4j.entity.Appointment;
import com.atguigu.java.ai.langchain4j.service.AppointmentService;
import com.atguigu.java.ai.langchain4j.utils.HttpUtils;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 预约挂号工具类
 * 提供预约挂号、取消预约和查询号源等工具函数给大模型在对应场景使用
 */
@Component
public class AppointmentTools {

    private static final Logger log = LoggerFactory.getLogger(AppointmentTools.class);

    /**
     * 预约服务
     */
    @Autowired
    private AppointmentService appointmentService;

    /**
     * 医院模拟系统接口地址，从配置读取，区分开发/测试/生产环境
     */
    @Value("${hospital.api.base-url:http://localhost:8201}")
    private String hospitalApiBaseUrl;

    /**
     * 预约挂号
     * 根据参数，先执行工具方法querySchedule查询是否可预约，并直接给用户回答是否可预约，并让用户确认所有预约信息，用户确认后再进行预约。
     *
     * @param appointment 预约信息对象，包含科室、医生、日期、时间等信息
     * @return 预约结果描述信息
     */
    @Tool(name="预约挂号", value = "根据参数，先执行工具方法querySchedule查询是否可预约，并直接给\n" +
            "用户回答是否可预约，并让用户确认所有预约信息，用户确认后再进行预约。如果用户没有提供具体的医生姓名，请从\n" +
            "向量存储中找到一位医生。")
    public String bookAppointment(Appointment appointment){

        /**
         * 查找数据库中是否包含对应的预约记录
         */
        Appointment appointmentDB = appointmentService.getOne(appointment);
        if(appointmentDB == null){
            /**
             * 防止大模型幻觉设置了id
             */
            appointment.setId(null);
            if(appointmentService.save(appointment)){//预约数据库中不存在该预约就添加
                return "预约成功，并返回预约详情";
            }else{
                return "预约失败";
            }
        }
        return "您在相同的科室和时间已有预约";
    }

    /**
     * 取消预约挂号
     * 根据参数，查询预约是否存在，如果存在则删除预约记录并返回取消预约成功，否则返回取消预约失败
     *
     * @param appointment 预约信息对象，用于匹配需要取消的预约记录
     * @return 取消预约结果描述信息
     */
    @Tool(name="取消预约挂号", value = "根据参数，查询预约是否存在，如果存在则删除预约记录并返回取消预约成功，否则返回取消预约失败")
    public String cancelAppointment(Appointment appointment){
        Appointment appointmentDB = appointmentService.getOne(appointment);
        if(appointmentDB != null){
            /**
             * 删除预约记录
             */
            if(appointmentService.removeById(appointmentDB.getId())){
                return "取消预约成功";
            }else{
                return "取消预约失败";
            }
        }
        /**
         * 取消失败
         */
        return "您没有预约记录，请核对预约科室和时间";
    }

    /**
     * 查询是否有号源
     * 根据科室名称，日期，时间和医生查询是否有号源，并返回给用户
     *
     * @param name 科室名称
     * @param date 日期
     * @param time 时间，可选值：上午、下午
     * @param doctorName 医生名称，可选参数
     * @return 是否有可用号源，true表示有号源，false表示无号源
     */
    @Tool(name = "查询是否有号源", value="根据科室名称，日期，时间和医生查询是否有号源，并返回给用户")
    public boolean querySchedule(
            @P(value = "科室名称") String name,//此注解是给大模型标注该参数对应的描述,从用户的输入中获取
            @P(value = "日期") String date,
            @P(value = "时间，可选值：上午、下午") String time,
            @P(value = "医生名称", required = false) String doctorName
    ) {
        log.info("查询是否有号源：科室={}，日期={}，时间={}，医生={}", name, date, time, doctorName);

        /**
         * TODO 维护医生的排班信息：
         * 1. 如果没有指定医生名字，则根据其他条件查询是否有可以预约的医生（有返回true，否则返回false）
         * 2. 如果指定了医生名字，则判断医生是否有排班（没有排班返回false）
         * 3. 如果有排班，则判断医生排班时间段是否已约满（约满返回false，有空闲时间返回true）
         */

        String host = hospitalApiBaseUrl;
        String path = "/api/hosp/selectSchedule";
        String method = "POST";
        //远程调用接口所需要的参数
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        //请求体中封装了查询参数
        bodys.put("name", name);
        if (doctorName != null && !doctorName.isBlank()) {
            bodys.put("doctorName", doctorName);
        }
        bodys.put("date", date);
        bodys.put("time", time);

        try {
            CloseableHttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity,"UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(result);
            Integer code = jsonObject.getInteger("code");
            if (code == null || code != 200) {
                return false;
            }
            // 平台 selectSchedule 返回 { code, message, data:[ {availableNumber, status, ...} ] }
            // 只有确实存在"状态可用且剩余号源大于 0"的排班，才算有号源；
            // 不能再把"接口调用成功"当成"有号"。
            JSONArray scheduleList = jsonObject.getJSONArray("data");
            if (scheduleList == null || scheduleList.isEmpty()) {
                return false;
            }
            for (int i = 0; i < scheduleList.size(); i++) {
                JSONObject schedule = scheduleList.getJSONObject(i);
                Integer availableNumber = schedule.getInteger("availableNumber");
                Integer status = schedule.getInteger("status");
                if (availableNumber != null && availableNumber > 0
                        && (status == null || status == 1)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            /**
             * 当远程服务不可用时，记录错误并返回false
             */
            log.error("查询号源接口调用失败，host={}", host, e);
        }
        /**
         * 服务不可用时默认返回false，表示无法确认是否有号源
         */
        return false;
    }
}
