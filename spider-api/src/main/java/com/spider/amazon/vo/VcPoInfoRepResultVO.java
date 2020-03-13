package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * @ClassName VcPoInfoRepResultVO
 * @Description VC Po页记录信息
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcPoInfoRepResultVO {
    private Long dataCount;
    private Integer pageNo;
    private Long pageSize;
    private List<VcPoInfoRepResultDataVO> dataResult;
}
