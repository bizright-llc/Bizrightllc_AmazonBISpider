package com.spider.amazon.handler.abs;

import com.spider.amazon.cons.GetDataOfTaskByOffsetOperaTypeEnum;

public abstract class AbstractGetDataByOffsetHandler {
    abstract public void getAllBzyDataByOffset(GetDataOfTaskByOffsetOperaTypeEnum operaTypeEnum,Object objDTO);
}
