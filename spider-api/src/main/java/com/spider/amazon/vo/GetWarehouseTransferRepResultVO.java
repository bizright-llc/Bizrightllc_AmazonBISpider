package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * @ClassName GetWarehouseTransferRepResultVO
 * @Description WarehouseTransfer页记录信息
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetWarehouseTransferRepResultVO {
    private Long dataCount;
    private Integer pageNo;
    private Long pageSize;
    private List<GetWarehouseTransferRepResultDataVO> dataResult;
}
