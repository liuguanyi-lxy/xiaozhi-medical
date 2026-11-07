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
    @Tool(name = "查询号源", value = "根据科室名称、日期、时段和医生查询号源。"
            + "返回值里包含该时段真实出诊医生的姓名、职称和剩余号数。"
            + "回答用户时，医生姓名、职称、时间、剩余号数只能照抄这里的返回值，不得自行补充或替换。")
    public String querySchedule(
            @P(value = "科室名称") String name,//此注解是给大模型标注该参数对应的描述,从用户的输入中获取
            @P(value = "日期") String date,
            @P(value = "时间，可选值：上午、下午") String time,
            @P(value = "医生名称", required = false) String doctorName
    ) {
        log.info("查询号源：科室={}，日期={}，时间={}，医生={}", name, date, time, doctorName);

        String host = hospitalApiBaseUrl;
        // 平台侧映射：HospitalApiController 类级 @RequestMapping("/api/hosp/hospital")
        // + 方法级 @PostMapping("selectSchedule")
        // => 真实路径必须是 /api/hosp/hospital/selectSchedule。
        String path = "/api/hosp/hospital/selectSchedule";
        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("name", name);
        if (doctorName != null && !doctorName.isBlank()) {
            bodys.put("doctorName", doctorName);
        }
        bodys.put("date", date);
        bodys.put("time", time);

        try {
            CloseableHttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(result);
            Integer code = jsonObject.getInteger("code");
            if (code == null || code != 200) {
                return "号源查询服务返回异常（code=" + code + "），我无法确认该时段是否有号";
            }

            JSONArray scheduleList = jsonObject.getJSONArray("data");
            if (scheduleList == null || scheduleList.isEmpty()) {
                return "该时段查无排班，即没有可预约的号源";
            }

            // 只收集"状态可用且剩余号源大于 0"的排班，并把真实医生信息原样带回给模型，
            // 避免模型只知道"有号"却不知道是谁出诊，从而自行编造医生姓名。
            StringBuilder available = new StringBuilder();
            for (int i = 0; i < scheduleList.size(); i++) {
                JSONObject schedule = scheduleList.getJSONObject(i);
                Integer availableNumber = schedule.getInteger("availableNumber");
                Integer status = schedule.getInteger("status");
                if (availableNumber != null && availableNumber > 0
                        && (status == null || status == 1)) {
                    if (available.length() > 0) {
                        available.append("；");
                    }
                    available.append(String.format("%s（%s），剩余 %d 个号",
                            schedule.getString("docname"),
                            schedule.getString("title"),
                            availableNumber));
                }
            }

            if (available.length() == 0) {
                return "该时段有排班，但号源已约满，没有可用号";
            }
            return "有可用号源：" + available
                    + "。以上医生与号数来自医院排班系统，请原样转述给用户。";

        } catch (Exception e) {
            log.error("查询号源接口调用失败，host={}", host, e);
            return "号源查询服务不可用，我无法确认该时段是否有号，请稍后再试或直接联系医院";
        }
    }
}
