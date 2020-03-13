package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName VcPoInfoRepVO
 * @Description VC Po请求返回实体
 */
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VcPoInfoRepVO {
    private ResponseHeader responseHeader;
    private String code;
    private String message;
    private VcPoInfoRepResultVO result;
}
