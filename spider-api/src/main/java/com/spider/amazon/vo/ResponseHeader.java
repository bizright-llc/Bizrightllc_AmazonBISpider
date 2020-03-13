package com.spider.amazon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName ResponseHeader
 * @Description 公共返回头
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseHeader implements Serializable{

    /**
     * Status code used to indicate an error, or "200" for success. required
     */
    private String statusCode;

    /**
     * Conveys failure codes from downstream entities or for more granular conveyance of specific error conditions.
     */
    private String subStatusCode;

    /**
     * Not parsed programmatically. Example "Downstream system offline"
     */
    private String statusMessage;

}
