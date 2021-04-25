package com.sunday.remark.controller;

import com.sunday.remark.config.SystemConstants;
import com.sunday.remark.controller.util.APIBody;
import com.sunday.remark.controller.util.RtnCodeEnum;
import com.sunday.remark.repository.entity.Remark;
import com.sunday.remark.service.RemarkService;
import com.sunday.remark.service.SecurityService;
import com.sunday.remark.service.exception.ExceedAuthorizedAccessException;
import net.sf.json.JSONArray;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


/**
 * 评价接口
 */
@RestController
@EnableCaching
public final class RemarkController {
    private final RemarkService remarkService;

    private final SecurityService securityService;

    public RemarkController(RemarkService remarkService, SecurityService securityService) {
        this.remarkService = remarkService;
        this.securityService = securityService;
    }

    /**
     * [CREATE] 发布评价
     */
    @PostMapping("/remark")
    public APIBody createRemark(@RequestBody Remark remark){
        remark.validateForCreate(securityService);

        try {
            if(remarkService.createRemark(remark)){
                return APIBody.buildSuccess();
            }
        } catch (ExceedAuthorizedAccessException e) {
            return APIBody.buildError(RtnCodeEnum.USER_ERROR_A0300);
        }

        return APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/remark")
    public APIBody deleteRemark(@RequestBody Remark remark){
        remark.validateForDelete();

        if (remarkService.deleteRemark(remark)) {
            return APIBody.buildSuccess();
        }

        return APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
    }


    /**
     * 查询商品关联的评价，一次查询固定的条目
     * @param itemId 商品id
     * @param start 当前查询坐标
     */
    @GetMapping("/remark")
    public APIBody listRemarks(String itemId, int start, Integer consumerId){
        Assert.isTrue(!StringUtils.isEmpty(itemId), "商品id不能为空");

        start--;
        JSONArray list = remarkService.listRemarks(itemId, start, start + SystemConstants.REMARK_MAX_LIST_LENGTH - 1);

        //原列表是从redis或db中读取的静态数据，而点赞数据每时每刻都在变化，分开获取这两个部分。
        return APIBody.buildSuccess(remarkService.appendDynamicVoteInfo(list, consumerId));
    }

    /**
     * 查询商品评分摘要
     * @param itemId 商品id
     */
    @GetMapping("/scores")
    public APIBody listScores(String itemId){
        Assert.isTrue(!StringUtils.isEmpty(itemId), "商品id不能为空");
        JSONArray list = remarkService.listScores(itemId);
        return APIBody.buildSuccess(list);
    }

    /**
     * 查询商品推荐评价
     * @param itemId 商品id
     * @param start 当前查询坐标
     */
    @GetMapping("/recommends")
    public APIBody listRecommends(String itemId, int start, Integer consumerId){
        Assert.isTrue(!StringUtils.isEmpty(itemId), "商品id不能为空");
        start--;
        JSONArray list = remarkService.listRecommendRemarks(itemId, start, start + SystemConstants.REMARK_MAX_LIST_LENGTH - 1);
        return APIBody.buildSuccess(remarkService.appendDynamicVoteInfo(list, consumerId));
    }
}